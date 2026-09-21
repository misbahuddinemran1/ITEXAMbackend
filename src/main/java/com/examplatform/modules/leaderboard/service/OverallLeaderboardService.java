package com.examplatform.modules.leaderboard.service;

import com.examplatform.common.exception.ValidationException;
import com.examplatform.modules.exam.entity.Exam;
import com.examplatform.modules.exam.entity.ExamAttemptHistory;
import com.examplatform.modules.exam.repository.ExamRepository;
import com.examplatform.modules.leaderboard.dto.LeaderboardEntryDto;
import com.examplatform.modules.leaderboard.dto.LeaderboardPageResponse;
import com.examplatform.modules.leaderboard.dto.LeaderboardSettingsUpdateRequest;
import com.examplatform.modules.leaderboard.dto.MyRankResponse;
import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import com.examplatform.modules.leaderboard.entity.UserLeaderboardStats;
import com.examplatform.modules.leaderboard.entity.UserMonthlyLeaderboardStats;
import com.examplatform.modules.leaderboard.repository.LeaderboardSettingsRepository;
import com.examplatform.modules.leaderboard.repository.UserLeaderboardStatsRepository;
import com.examplatform.modules.leaderboard.repository.UserMonthlyLeaderboardStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Live Exam এর Overall + Monthly leaderboard।
 * "Level" বলতে এই প্রজেক্টে ছাত্রের Target Exam (Exam Category এর code) বোঝায়।
 * র‍্যাংক হয় সব Live Exam এর score percent যোগ করে (বেশি exam দিলে সুবিধা)।
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OverallLeaderboardService {

    private static final String SETTINGS_ID = "default";
    private static final int MAX_PAGE_SIZE = 100;
    private static final DateTimeFormatter YM_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

    // Leaderboard আপডেট আলাদা একটা thread এ, একটার পর একটা চলে।
    // এতে exam জমা দেওয়ার transaction বা DB connection কখনো আটকায় না।
    private static final ExecutorService UPDATER = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "leaderboard-updater");
        t.setDaemon(true);
        return t;
    });

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    private final LeaderboardSettingsRepository settingsRepository;
    private final UserLeaderboardStatsRepository statsRepository;
    private final UserMonthlyLeaderboardStatsRepository monthlyStatsRepository;
    private final ExamRepository examRepository;

    // ============================================
    // TRIGGER: Live exam জমা হওয়ার পর LiveExamService এটা ডাকে
    // ============================================
    // exam এর transaction সফলভাবে commit হলে তবেই আপডেট চলে (rollback হলে কিছুই গোনা হয় না)।
    // যেকোনো সমস্যা এখানেই লগ হয়, exam জমা দেওয়াকে কখনো আটকায় না।
    public void scheduleStatsUpdate(String userId, ExamAttemptHistory history) {
        try {
            Runnable task = () -> {
                try {
                    transactionTemplate.executeWithoutResult(status -> applyAttempt(userId, history));
                } catch (Exception e) {
                    log.warn("Leaderboard update failed for user {}: {}", userId, e.getMessage());
                }
            };

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        UPDATER.submit(task);
                    }
                });
            } else {
                UPDATER.submit(task);
            }
        } catch (Exception e) {
            log.warn("Could not schedule leaderboard update for user {}: {}", userId, e.getMessage());
        }
    }

    private void applyAttempt(String userId, ExamAttemptHistory history) {
        String level = fetchUserLevel(userId);
        if (!isEligible(level)) {
            log.info("Leaderboard skip: user {} এর Target Exam সেট করা নেই", userId);
            return;
        }

        BigDecimal scorePercent = history.getPercentage() != null ? history.getPercentage() : BigDecimal.ZERO;
        LocalDateTime submittedAt = history.getSubmittedAt() != null ? history.getSubmittedAt() : LocalDateTime.now();
        String yearMonth = submittedAt.toLocalDate().format(YM_FORMAT);

        updateOverallStats(userId, level, scorePercent);
        updateMonthlyStats(userId, level, yearMonth, scorePercent);
    }

    private void updateOverallStats(String userId, String level, BigDecimal scorePercent) {
        UserLeaderboardStats stats = statsRepository.findByUserId(userId)
                .orElseGet(() -> UserLeaderboardStats.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .educationLevel(level)
                        .totalExamsTaken(0)
                        .totalPoints(BigDecimal.ZERO)
                        .avgScorePercent(BigDecimal.ZERO)
                        .build());

        stats.setEducationLevel(level); // Target Exam বদলালে sync থাকবে
        int newCount = stats.getTotalExamsTaken() + 1;
        BigDecimal newTotalPoints = stats.getTotalPoints().add(scorePercent);
        BigDecimal newAvg = newTotalPoints.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        stats.setTotalExamsTaken(newCount);
        stats.setTotalPoints(newTotalPoints);
        stats.setAvgScorePercent(newAvg);
        stats.setLastUpdatedAt(LocalDateTime.now());

        statsRepository.save(stats);
    }

    private void updateMonthlyStats(String userId, String level, String yearMonth, BigDecimal scorePercent) {
        UserMonthlyLeaderboardStats stats = monthlyStatsRepository.findByUserIdAndYearMonth(userId, yearMonth)
                .orElseGet(() -> UserMonthlyLeaderboardStats.builder()
                        .id(UUID.randomUUID().toString())
                        .userId(userId)
                        .educationLevel(level)
                        .yearMonth(yearMonth)
                        .examsTakenThisMonth(0)
                        .totalPointsThisMonth(BigDecimal.ZERO)
                        .avgScorePercentThisMonth(BigDecimal.ZERO)
                        .build());

        stats.setEducationLevel(level);
        int newCount = stats.getExamsTakenThisMonth() + 1;
        BigDecimal newTotalPoints = stats.getTotalPointsThisMonth().add(scorePercent);
        BigDecimal newAvg = newTotalPoints.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        stats.setExamsTakenThisMonth(newCount);
        stats.setTotalPointsThisMonth(newTotalPoints);
        stats.setAvgScorePercentThisMonth(newAvg);
        stats.setLastUpdatedAt(LocalDateTime.now());

        monthlyStatsRepository.save(stats);
    }

    // ============================================
    // OVERALL LEADERBOARD (ছাত্রের নিজের Target Exam অনুযায়ী)
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardPageResponse getOverallLeaderboard(String requestingUserId, int page, int size) {
        LeaderboardSettings settings = getSettingsOrDefault();
        if (!settings.isEnabled()) {
            return LeaderboardPageResponse.disabled();
        }

        String userLevel = fetchUserLevel(requestingUserId);
        if (!isEligible(userLevel)) {
            return LeaderboardPageResponse.needsProfileCompletion();
        }

        int safePage = Math.max(page, 0);
        int safeSize = clampSize(size);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        boolean levelWise = settings.isLevelWiseSeparate();

        Page<UserLeaderboardStats> result = levelWise
                ? statsRepository.findByEducationLevelAndTotalExamsTakenGreaterThanEqualOrderByTotalPointsDesc(
                        userLevel, settings.getOverallMinExamsRequired(), pageable)
                : statsRepository.findByTotalExamsTakenGreaterThanEqualOrderByTotalPointsDesc(
                        settings.getOverallMinExamsRequired(), pageable);

        List<LeaderboardEntryDto> entries = buildEntries(
                result.getContent().stream()
                        .map(s -> new EntryRow(s.getUserId(), s.getTotalPoints(), s.getAvgScorePercent(), s.getTotalExamsTaken()))
                        .toList(),
                requestingUserId, safePage, safeSize);

        Integer myRank = entries.stream()
                .filter(LeaderboardEntryDto::isCurrentUser)
                .map(LeaderboardEntryDto::getRank)
                .findFirst()
                .orElse(null); // এই পাতায় না থাকলে null, /my-rank আলাদা ভাবে জানাবে

        return LeaderboardPageResponse.builder()
                .status("OK")
                .educationLevel(levelWise ? userLevel : "ALL")
                .entries(entries)
                .totalElements(result.getTotalElements())
                .myRank(myRank)
                .build();
    }

    // ============================================
    // MONTHLY LEADERBOARD
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardPageResponse getMonthlyLeaderboard(String requestingUserId, String yearMonth, int page, int size) {
        LeaderboardSettings settings = getSettingsOrDefault();
        if (!settings.isEnabled()) {
            return LeaderboardPageResponse.disabled();
        }

        String userLevel = fetchUserLevel(requestingUserId);
        if (!isEligible(userLevel)) {
            return LeaderboardPageResponse.needsProfileCompletion();
        }

        String targetMonth = normalizeYearMonth(yearMonth);
        int requiredExams = resolveMonthlyRequiredExams(settings, targetMonth);

        int safePage = Math.max(page, 0);
        int safeSize = clampSize(size);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        boolean levelWise = settings.isLevelWiseSeparate();

        Page<UserMonthlyLeaderboardStats> result = levelWise
                ? monthlyStatsRepository
                        .findByEducationLevelAndYearMonthAndExamsTakenThisMonthGreaterThanEqualOrderByTotalPointsThisMonthDesc(
                                userLevel, targetMonth, requiredExams, pageable)
                : monthlyStatsRepository
                        .findByYearMonthAndExamsTakenThisMonthGreaterThanEqualOrderByTotalPointsThisMonthDesc(
                                targetMonth, requiredExams, pageable);

        List<LeaderboardEntryDto> entries = buildEntries(
                result.getContent().stream()
                        .map(s -> new EntryRow(s.getUserId(), s.getTotalPointsThisMonth(),
                                s.getAvgScorePercentThisMonth(), s.getExamsTakenThisMonth()))
                        .toList(),
                requestingUserId, safePage, safeSize);

        return LeaderboardPageResponse.builder()
                .status("OK")
                .educationLevel(levelWise ? userLevel : "ALL")
                .yearMonth(targetMonth)
                .requiredExamsThisMonth(requiredExams)
                .entries(entries)
                .totalElements(result.getTotalElements())
                .build();
    }

    // RELATIVE হলে "এই মাসের মোট published exam - allowedMissed" হিসাব করে
    private int resolveMonthlyRequiredExams(LeaderboardSettings settings, String yearMonth) {
        if (settings.getMonthlyThresholdType() == LeaderboardSettings.ThresholdType.FIXED) {
            return settings.getMonthlyMinExamsRequired();
        }
        YearMonth ym = YearMonth.parse(yearMonth, YM_FORMAT);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        long totalExamsThisMonth = examRepository.countByPublishStatusAndExamDateBetween(
                Exam.PublishStatus.PUBLISHED, start, end);
        int required = (int) totalExamsThisMonth - settings.getMonthlyAllowedMissedExams();
        return Math.max(required, 1); // অন্তত ১টা তো লাগবেই
    }

    // ============================================
    // MY RANK (eligibility বার্তা সহ)
    // ============================================
    @Transactional(readOnly = true)
    public MyRankResponse getMyOverallRank(String userId) {
        LeaderboardSettings settings = getSettingsOrDefault();
        if (!settings.isEnabled()) {
            return MyRankResponse.builder()
                    .status("DISABLED")
                    .message("Leaderboard এখন বন্ধ আছে।")
                    .build();
        }

        String userLevel = fetchUserLevel(userId);
        if (!isEligible(userLevel)) {
            return MyRankResponse.needsProfileCompletion();
        }

        Optional<UserLeaderboardStats> statsOpt = statsRepository.findByUserId(userId);
        if (statsOpt.isEmpty()) {
            return MyRankResponse.builder()
                    .status("NOT_STARTED")
                    .examsTaken(0)
                    .examsNeededMore(settings.getOverallMinExamsRequired())
                    .message("Leaderboard-এ আসতে " + settings.getOverallMinExamsRequired() + "টা Live Exam দিন।")
                    .build();
        }

        UserLeaderboardStats stats = statsOpt.get();
        boolean eligible = stats.getTotalExamsTaken() >= settings.getOverallMinExamsRequired();

        if (!eligible) {
            int needed = settings.getOverallMinExamsRequired() - stats.getTotalExamsTaken();
            return MyRankResponse.builder()
                    .status("NOT_ELIGIBLE")
                    .examsTaken(stats.getTotalExamsTaken())
                    .examsNeededMore(needed)
                    .totalPoints(stats.getTotalPoints())
                    .message("আরও " + needed + "টা Live Exam দিন Leaderboard-এ rank পেতে।")
                    .build();
        }

        int rank = computeRank(userLevel, stats.getTotalPoints(),
                settings.getOverallMinExamsRequired(), settings.isLevelWiseSeparate());

        return MyRankResponse.builder()
                .status("OK")
                .rank(rank)
                .examsTaken(stats.getTotalExamsTaken())
                .totalPoints(stats.getTotalPoints())
                .avgScorePercent(stats.getAvgScorePercent())
                .build();
    }

    private int computeRank(String level, BigDecimal myPoints, int minExams, boolean levelWise) {
        Long higherCount;
        if (levelWise) {
            higherCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_leaderboard_stats "
                            + "WHERE education_level = ? AND total_exams_taken >= ? AND total_points > ?",
                    Long.class, level, minExams, myPoints);
        } else {
            higherCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM user_leaderboard_stats "
                            + "WHERE total_exams_taken >= ? AND total_points > ?",
                    Long.class, minExams, myPoints);
        }
        return (higherCount == null ? 0 : higherCount.intValue()) + 1;
    }

    // ============================================
    // ADMIN SETTINGS
    // ============================================
    @Transactional(readOnly = true)
    public LeaderboardSettings getSettings() {
        return getSettingsOrDefault();
    }

    @Transactional
    public LeaderboardSettings updateSettings(LeaderboardSettingsUpdateRequest req, String adminId) {
        if (req.getMonthlyThresholdType() == null) {
            throw new ValidationException("Threshold type দিতে হবে");
        }
        if (req.getOverallMinExamsRequired() < 0
                || req.getMonthlyMinExamsRequired() < 0
                || req.getMonthlyAllowedMissedExams() < 0) {
            throw new ValidationException("Exam সংখ্যা ০ বা তার বেশি হতে হবে");
        }

        LeaderboardSettings settings = getSettingsOrDefault();
        settings.setOverallMinExamsRequired(req.getOverallMinExamsRequired());
        settings.setMonthlyThresholdType(req.getMonthlyThresholdType());
        settings.setMonthlyMinExamsRequired(req.getMonthlyMinExamsRequired());
        settings.setMonthlyAllowedMissedExams(req.getMonthlyAllowedMissedExams());
        settings.setLevelWiseSeparate(req.isLevelWiseSeparate());
        settings.setEnabled(req.isEnabled());
        settings.setUpdatedByAdminId(shorten(adminId, 36));
        return settingsRepository.save(settings);
    }

    private LeaderboardSettings getSettingsOrDefault() {
        return settingsRepository.findById(SETTINGS_ID)
                .orElseGet(() -> settingsRepository.save(
                        LeaderboardSettings.builder().id(SETTINGS_ID).build()));
    }

    // ============================================
    // HELPERS
    // ============================================
    private record EntryRow(String userId, BigDecimal points, BigDecimal avgPercent, int examsTaken) {}

    private boolean isEligible(String level) {
        return level != null && !level.isBlank();
    }

    private int clampSize(int size) {
        return Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    }

    private String shorten(String value, int max) {
        if (value == null) return null;
        return value.length() > max ? value.substring(0, max) : value;
    }

    private String normalizeYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now().format(YM_FORMAT);
        }
        try {
            return YearMonth.parse(yearMonth.trim(), YM_FORMAT).format(YM_FORMAT);
        } catch (DateTimeParseException e) {
            return YearMonth.now().format(YM_FORMAT);
        }
    }

    // ছাত্রের Target Exam (Exam Category code)
    private String fetchUserLevel(String userId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT target_exam FROM users WHERE id = ?", userId);
            if (rows.isEmpty()) return null;
            return (String) rows.get(0).get("target_exam");
        } catch (Exception e) {
            log.warn("Could not fetch target_exam for user {}", userId);
            return null;
        }
    }

    private List<LeaderboardEntryDto> buildEntries(List<EntryRow> rows, String requestingUserId, int page, int size) {
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = page * size + 1;
        for (EntryRow r : rows) {
            Map<String, String> userInfo = fetchUserNameAndCollege(r.userId());
            entries.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .userId(r.userId())
                    .userName(userInfo.getOrDefault("name", ""))
                    .collegeName(userInfo.getOrDefault("college", ""))
                    .totalPoints(r.points())
                    .avgScorePercent(r.avgPercent())
                    .examsTaken(r.examsTaken())
                    .isCurrentUser(r.userId().equals(requestingUserId))
                    .build());
        }
        return entries;
    }

    private Map<String, String> fetchUserNameAndCollege(String userId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT full_name, full_name_bn, institution_name FROM users WHERE id = ?", userId);
            if (rows.isEmpty()) return Map.of();
            Map<String, Object> row = rows.get(0);
            String fullNameBn = (String) row.get("full_name_bn");
            String fullName = (String) row.get("full_name");
            String college = (String) row.get("institution_name");
            String name = (fullNameBn != null && !fullNameBn.isBlank()) ? fullNameBn : fullName;
            return Map.of(
                    "name", name == null ? "" : name,
                    "college", college == null ? "" : college);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
