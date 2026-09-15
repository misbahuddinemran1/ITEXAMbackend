package com.examplatform.modules.challenge.service;

import com.examplatform.modules.challenge.dto.*;
import com.examplatform.modules.challenge.entity.*;
import com.examplatform.modules.challenge.repository.*;
import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import com.examplatform.modules.guide.entity.GuidePracticeMcqOption;
import com.examplatform.modules.guide.repository.GuidePracticeMcqRepository;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChallengeService {

        private static final int POINTS_PER_CORRECT = 2;
    private static final int WIN_BONUS = 10;
    private static final int DRAW_BONUS = 3;
    private static final int CHALLENGE_EXPIRY_HOURS = 48;
    private static final int MIN_QUESTION_COUNT = 5;
    private static final int MAX_QUESTION_COUNT = 30;
    private static final int DEFAULT_QUESTION_COUNT = 10;

    private int resolveQuestionCount(Integer requested) {
        int count = requested != null ? requested : DEFAULT_QUESTION_COUNT;
        if (count < MIN_QUESTION_COUNT) count = MIN_QUESTION_COUNT;
        if (count > MAX_QUESTION_COUNT) count = MAX_QUESTION_COUNT;
        return count;
    }

    private final ChallengeRepository challengeRepository;
    private final ChallengeQuestionRepository challengeQuestionRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ChallengeResultRepository challengeResultRepository;
    private final UserChallengeStatsRepository userChallengeStatsRepository;
    private final GuidePracticeMcqRepository guidePracticeMcqRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    // ---------- Friend Challenge তৈরি ----------

            @Transactional
    public ChallengeDetailResponse createFriendChallenge(String creatorId, CreateFriendChallengeRequest req) {
        User creator = getUser(creatorId);
        User opponent = getUser(req.getOpponentId());

        if (challengeRepository.findPendingBetween(creatorId, req.getOpponentId()).isPresent()) {
            throw new IllegalStateException(
                    "এই বন্ধুর সাথে ইতিমধ্যে একটি চ্যালেঞ্জ অপেক্ষমান আছে, সেটি গ্রহণ/প্রত্যাখ্যান না হওয়া পর্যন্ত নতুন চ্যালেঞ্জ পাঠানো যাবে না");
        }

        Chapter chapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));
        Topic topic = req.getTopicId() != null
                ? topicRepository.findById(req.getTopicId()).orElse(null)
                : null;
                int questionCount = resolveQuestionCount(req.getQuestionCount());

        Challenge challenge = Challenge.builder()
                .mode(Challenge.Mode.FRIEND)
                .status(Challenge.Status.PENDING)
                .creator(creator)
                .opponent(opponent)
                .chapter(chapter)
                .topic(topic)
                .questionCount(questionCount)
                .expiresAt(LocalDateTime.now().plusHours(CHALLENGE_EXPIRY_HOURS))
                .build();
        challenge = challengeRepository.save(challenge);

        buildQuestionSet(challenge, chapter, topic, questionCount);

        return toDetailResponse(challenge, creatorId);
    }

    // ---------- Friend Challenge Accept/Decline ----------
    @Transactional
    public ChallengeDetailResponse acceptChallenge(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        validateParticipant(challenge, userId);
        if (challenge.getStatus() != Challenge.Status.PENDING) {
            throw new IllegalStateException("এই চ্যালেঞ্জ আর গ্রহণযোগ্য নয়");
        }
        challenge.setStatus(Challenge.Status.ACTIVE);
        challengeRepository.save(challenge);
        return toDetailResponse(challenge, userId);
    }

    @Transactional
    public void declineChallenge(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        validateParticipant(challenge, userId);
        challenge.setStatus(Challenge.Status.DECLINED);
        challengeRepository.save(challenge);
    }

    // ---------- Random Matchmaking ----------
    @Transactional
    public Map<String, Object> quickMatch(String userId, QuickMatchRequest req) {
                int questionCount = resolveQuestionCount(req.getQuestionCount());

        List<Challenge> waiting = challengeRepository.findWaitingRandomMatch(
                userId, req.getChapterId(), req.getTopicId(), questionCount);

        if (!waiting.isEmpty()) {
            // ম্যাচ পাওয়া গেছে — সেই চ্যালেঞ্জে opponent হিসেবে যুক্ত হও
            Challenge challenge = waiting.get(0);
            challenge.setOpponent(getUser(userId));
            challenge.setStatus(Challenge.Status.ACTIVE);
            challengeRepository.save(challenge);

            Map<String, Object> result = new HashMap<>();
            result.put("matched", true);
            result.put("challenge", toDetailResponse(challenge, userId));
            return result;
        }

        // ম্যাচ নেই — নতুন waiting entry তৈরি করে pool এ রাখো
        Chapter chapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));
        Topic topic = req.getTopicId() != null
                ? topicRepository.findById(req.getTopicId()).orElse(null)
                : null;

        Challenge challenge = Challenge.builder()
                .mode(Challenge.Mode.RANDOM)
                .status(Challenge.Status.PENDING)
                .creator(getUser(userId))
                .opponent(null)
                .chapter(chapter)
                .topic(topic)
                .questionCount(questionCount)
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // waiting pool ৫ মিনিট পর expire
                .build();
        challenge = challengeRepository.save(challenge);
        buildQuestionSet(challenge, chapter, topic, questionCount);

        Map<String, Object> result = new HashMap<>();
        result.put("matched", false);
        result.put("challengeId", challenge.getId());
        return result;
    }

    // Client প্রতি ২-৩ সেকেন্ডে এটা poll করবে matched হয়েছে কিনা জানতে
    public Map<String, Object> checkMatchStatus(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        Map<String, Object> result = new HashMap<>();
        boolean matched = challenge.getStatus() == Challenge.Status.ACTIVE;
        result.put("matched", matched);
        if (matched) {
            result.put("challenge", toDetailResponse(challenge, userId));
        }
        return result;
    }
            // ---------- Pending Friend Challenge Edit ----------
    @Transactional
    public ChallengeDetailResponse editFriendChallenge(String userId, String challengeId, CreateFriendChallengeRequest req) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        if (!challenge.getCreator().getId().equals(userId)) {
            throw new SecurityException("শুধু যে চ্যালেঞ্জ পাঠিয়েছে সে-ই এটি এডিট করতে পারবে");
        }
        if (challenge.getStatus() != Challenge.Status.PENDING) {
            throw new IllegalStateException("গৃহীত/সম্পন্ন চ্যালেঞ্জ এডিট করা যাবে না");
        }

        Chapter chapter = chapterRepository.findById(req.getChapterId())
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found"));
        Topic topic = req.getTopicId() != null
                ? topicRepository.findById(req.getTopicId()).orElse(null)
                : null;
        int questionCount = resolveQuestionCount(req.getQuestionCount());

        challenge.setChapter(chapter);
        challenge.setTopic(topic);
        challenge.setQuestionCount(questionCount);
        challengeRepository.save(challenge);

        // পুরনো প্রশ্ন সেট মুছে নতুন করে বানানো হচ্ছে
        List<ChallengeQuestion> oldQuestions = challengeQuestionRepository.findByChallengeIdOrderByOrderIndexAsc(challengeId);
        challengeQuestionRepository.deleteAll(oldQuestions);
        buildQuestionSet(challenge, chapter, topic, questionCount);

        return toDetailResponse(challenge, userId);
    }

    // ---------- Pending Friend Challenge Delete ----------
    @Transactional
    public void deleteFriendChallenge(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        if (!challenge.getCreator().getId().equals(userId)) {
            throw new SecurityException("শুধু যে চ্যালেঞ্জ পাঠিয়েছে সে-ই এটি মুছতে পারবে");
        }
        if (challenge.getStatus() != Challenge.Status.PENDING) {
            throw new IllegalStateException("গৃহীত/সম্পন্ন চ্যালেঞ্জ মুছে ফেলা যাবে না");
        }
        challengeRepository.delete(challenge); // ON DELETE CASCADE দিয়ে questions ও মুছে যাবে
    }

    // ---------- প্রশ্ন সেট তৈরি (creation সময়ে একবারই) ----------
    private void buildQuestionSet(Challenge challenge, Chapter chapter, Topic topic, int questionCount) {
        List<GuidePracticeMcq> pool;
        if (topic != null) {
            pool = guidePracticeMcqRepository.findByTopicId(topic.getId());
        } else {
           pool = guidePracticeMcqRepository.findByTopic_Chapter_Id(chapter.getId());
        }

        Collections.shuffle(pool);
        List<GuidePracticeMcq> selected = pool.stream()
                .limit(Math.min(questionCount, pool.size()))
                .collect(Collectors.toList());

        int order = 0;
        List<ChallengeQuestion> questions = new ArrayList<>();
        for (GuidePracticeMcq mcq : selected) {
            questions.add(ChallengeQuestion.builder()
                    .challenge(challenge)
                    .mcq(mcq)
                    .orderIndex(order++)
                    .build());
        }
        challengeQuestionRepository.saveAll(questions);
    }

    // ---------- Challenge Detail (প্রশ্নসহ) ----------
    public ChallengeDetailResponse getChallengeDetail(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        validateParticipant(challenge, userId);
        return toDetailResponse(challenge, userId);
    }

    // ---------- একটা প্রশ্নের উত্তর সাবমিট ----------
    @Transactional
    public Map<String, Object> submitAttempt(String userId, String challengeId, SubmitAttemptRequest req) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        validateParticipant(challenge, userId);
        if (challenge.getStatus() != Challenge.Status.ACTIVE) {
            throw new IllegalStateException("এই চ্যালেঞ্জ এখন সক্রিয় নয়");
        }

        if (challengeAttemptRepository.findByChallengeIdAndUserIdAndMcqId(challengeId, userId, req.getMcqId()).isPresent()) {
            throw new IllegalStateException("এই প্রশ্নের উত্তর আগেই দেওয়া হয়েছে");
        }

        long alreadyAnswered = challengeAttemptRepository.countByChallengeIdAndUserId(challengeId, userId);
        if (alreadyAnswered >= challenge.getQuestionCount()) {
            throw new IllegalStateException("আপনি ইতিমধ্যে এই চ্যালেঞ্জের সব প্রশ্নের উত্তর দিয়ে ফেলেছেন");
        }

        GuidePracticeMcq mcq = guidePracticeMcqRepository.findById(req.getMcqId())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        boolean isCorrect = false;
        GuidePracticeMcqOption selected = null;
        if (req.getSelectedOptionId() != null) {
            selected = mcq.getOptions().stream()
                    .filter(o -> o.getId().equals(req.getSelectedOptionId()))
                    .findFirst()
                    .orElse(null);
            isCorrect = selected != null && selected.isCorrect();
        }

        ChallengeAttempt attempt = ChallengeAttempt.builder()
                .challenge(challenge)
                .user(getUser(userId))
                .mcq(mcq)
                .selectedOption(selected)
                .isCorrect(isCorrect)
                .timeTakenMs(req.getTimeTakenMs())
                .build();
        challengeAttemptRepository.save(attempt);

        long answeredCount = challengeAttemptRepository.countByChallengeIdAndUserId(challengeId, userId);
        boolean myTurnDone = answeredCount >= challenge.getQuestionCount();

        if (myTurnDone) {
            maybeFinalizeChallenge(challenge);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("isCorrect", isCorrect);
        result.put("myTurnDone", myTurnDone);
        return result;
    }

    // দুইজনই শেষ করলে result গণনা করে ফাইনালাইজ করবে
    @Transactional
    protected void maybeFinalizeChallenge(Challenge challenge) {
        String creatorId = challenge.getCreator().getId();
        String opponentId = challenge.getOpponent() != null ? challenge.getOpponent().getId() : null;
        if (opponentId == null) return;

        long creatorAnswered = challengeAttemptRepository.countByChallengeIdAndUserId(challenge.getId(), creatorId);
        long opponentAnswered = challengeAttemptRepository.countByChallengeIdAndUserId(challenge.getId(), opponentId);

        if (creatorAnswered < challenge.getQuestionCount() || opponentAnswered < challenge.getQuestionCount()) {
            return; // এখনো দুইজনই শেষ করেনি
        }

        if (challengeResultRepository.findByChallengeId(challenge.getId()).isPresent()) {
            return; // আগেই ফাইনালাইজ হয়ে গেছে
        }

        int creatorScore = (int) challengeAttemptRepository
                .findByChallengeIdAndUserId(challenge.getId(), creatorId)
                .stream().filter(ChallengeAttempt::isCorrect).count();
        int opponentScore = (int) challengeAttemptRepository
                .findByChallengeIdAndUserId(challenge.getId(), opponentId)
                .stream().filter(ChallengeAttempt::isCorrect).count();

        String winnerId = null;
        int creatorPoints = creatorScore * POINTS_PER_CORRECT;
        int opponentPoints = opponentScore * POINTS_PER_CORRECT;

        if (creatorScore > opponentScore) {
            winnerId = creatorId;
            creatorPoints += WIN_BONUS;
        } else if (opponentScore > creatorScore) {
            winnerId = opponentId;
            opponentPoints += WIN_BONUS;
        } else {
            creatorPoints += DRAW_BONUS;
            opponentPoints += DRAW_BONUS;
        }

        ChallengeResult result = ChallengeResult.builder()
                .challenge(challenge)
                .winner(winnerId != null ? getUser(winnerId) : null)
                .creatorScore(creatorScore)
                .opponentScore(opponentScore)
                .creatorPointsEarned(creatorPoints)
                .opponentPointsEarned(opponentPoints)
                .build();
        challengeResultRepository.save(result);

        challenge.setStatus(Challenge.Status.COMPLETED);
        challenge.setCompletedAt(LocalDateTime.now());
        challengeRepository.save(challenge);

        updateStats(creatorId, creatorPoints, winnerId == null ? "DRAW" : winnerId.equals(creatorId) ? "WIN" : "LOSS");
        updateStats(opponentId, opponentPoints, winnerId == null ? "DRAW" : winnerId.equals(opponentId) ? "WIN" : "LOSS");
    }

    private void updateStats(String userId, int pointsEarned, String outcome) {
        UserChallengeStats stats = userChallengeStatsRepository.findByUserId(userId)
                .orElseGet(() -> UserChallengeStats.builder()
                        .userId(userId)
                        .user(getUser(userId))
                        .build());

        stats.setTotalPoints(stats.getTotalPoints() + pointsEarned);
        stats.setTotalPlayed(stats.getTotalPlayed() + 1);

        switch (outcome) {
            case "WIN" -> {
                stats.setTotalWins(stats.getTotalWins() + 1);
                stats.setCurrentWinStreak(stats.getCurrentWinStreak() + 1);
                stats.setBestWinStreak(Math.max(stats.getBestWinStreak(), stats.getCurrentWinStreak()));
            }
            case "LOSS" -> {
                stats.setTotalLosses(stats.getTotalLosses() + 1);
                stats.setCurrentWinStreak(0);
            }
            case "DRAW" -> {
                stats.setTotalDraws(stats.getTotalDraws() + 1);
                stats.setCurrentWinStreak(0);
            }
        }
        userChallengeStatsRepository.save(stats);
    }

    // ---------- Result দেখা ----------
    public ChallengeResultResponse getResult(String userId, String challengeId) {
        Challenge challenge = getChallengeOrThrow(challengeId);
        validateParticipant(challenge, userId);
        ChallengeResult result = challengeResultRepository.findByChallengeId(challengeId)
                .orElseThrow(() -> new IllegalStateException("প্রতিপক্ষের অপেক্ষায়, এখনো ফলাফল প্রস্তুত হয়নি"));

        String creatorId = challenge.getCreator().getId();
        String opponentId = challenge.getOpponent().getId();

        return ChallengeResultResponse.builder()
                .challengeId(challengeId)
                .winnerId(result.getWinner() != null ? result.getWinner().getId() : null)
                .creatorId(creatorId)
                .creatorName(challenge.getCreator().getFullNameBn() != null ? challenge.getCreator().getFullNameBn() : challenge.getCreator().getFullName())
                .creatorScore(result.getCreatorScore())
                .creatorPointsEarned(result.getCreatorPointsEarned())
                .opponentId(opponentId)
                .opponentName(challenge.getOpponent().getFullNameBn() != null ? challenge.getOpponent().getFullNameBn() : challenge.getOpponent().getFullName())
                .opponentScore(result.getOpponentScore())
                .opponentPointsEarned(result.getOpponentPointsEarned())
                .isDraw(result.getWinner() == null)
                .build();
    }

    // ---------- আমার চ্যালেঞ্জ লিস্ট ----------
    public List<ChallengeDetailResponse> getMyChallenges(String userId, String status) {
        Challenge.Status statusEnum = status != null ? Challenge.Status.valueOf(status) : null;
        return challengeRepository.findMyChallenges(userId, statusEnum).stream()
                .filter(c -> c.getOpponent() != null || c.getCreator().getId().equals(userId))
                .map(c -> toDetailResponse(c, userId))
                .collect(Collectors.toList());
    }

    // ---------- Leaderboard ----------
    public List<LeaderboardEntryResponse> getLeaderboard(int limit) {
        List<UserChallengeStats> top = userChallengeStatsRepository.findLeaderboard(PageRequest.of(0, limit));
        List<LeaderboardEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (UserChallengeStats s : top) {
            result.add(LeaderboardEntryResponse.builder()
                    .rank(rank++)
                    .userId(s.getUserId())
                    .userName(s.getUser().getFullNameBn() != null ? s.getUser().getFullNameBn() : s.getUser().getFullName())
                    .totalPoints(s.getTotalPoints())
                    .totalWins(s.getTotalWins())
                    .totalPlayed(s.getTotalPlayed())
                    .currentWinStreak(s.getCurrentWinStreak())
                    .build());
        }
        return result;
    }

    public Map<String, Object> getMyStats(String userId) {
        UserChallengeStats stats = userChallengeStatsRepository.findByUserId(userId)
                .orElse(UserChallengeStats.builder().userId(userId).build());
        Map<String, Object> result = new HashMap<>();
        result.put("totalPoints", stats.getTotalPoints());
        result.put("totalWins", stats.getTotalWins());
        result.put("totalLosses", stats.getTotalLosses());
        result.put("totalDraws", stats.getTotalDraws());
        result.put("totalPlayed", stats.getTotalPlayed());
        result.put("currentWinStreak", stats.getCurrentWinStreak());
        result.put("bestWinStreak", stats.getBestWinStreak());
        return result;
    }

    // ---------- Helpers ----------
    private User getUser(String id) {
        return userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
    }

    private Challenge getChallengeOrThrow(String id) {
        return challengeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
    }

    private void validateParticipant(Challenge challenge, String userId) {
        boolean isCreator = challenge.getCreator().getId().equals(userId);
        boolean isOpponent = challenge.getOpponent() != null && challenge.getOpponent().getId().equals(userId);
        if (!isCreator && !isOpponent) {
            throw new SecurityException("এই চ্যালেঞ্জে আপনার অ্যাক্সেস নেই");
        }
    }

    private ChallengeDetailResponse toDetailResponse(Challenge challenge, String requestingUserId) {
        boolean isMeCreator = challenge.getCreator().getId().equals(requestingUserId);

        List<ChallengeQuestionResponse> questions = challengeQuestionRepository
                .findByChallengeIdOrderByOrderIndexAsc(challenge.getId())
                .stream()
                .map(cq -> {
                    GuidePracticeMcq mcq = cq.getMcq();
                    List<ChallengeQuestionResponse.OptionItem> options = mcq.getOptions().stream()
                            .map(o -> ChallengeQuestionResponse.OptionItem.builder()
                                    .optionId(o.getId())
                                    .optionKey(o.getOptionKey())
                                    .optionText(o.getOptionText())
                                    .optionTextBn(o.getOptionTextBn())
                                    .build())
                            .collect(Collectors.toList());
                    return ChallengeQuestionResponse.builder()
                            .mcqId(mcq.getId())
                            .orderIndex(cq.getOrderIndex())
                            .questionText(mcq.getQuestionText())
                            .questionTextBn(mcq.getQuestionTextBn())
                            .options(options)
                            .build();
                })
                .collect(Collectors.toList());

        // requesting user নিজে যেসব mcqId-র উত্তর দিয়ে ফেলেছে, তার লিস্ট (frontend resume করার জন্য)
        List<String> myAnsweredMcqIds = challengeAttemptRepository
                .findByChallengeIdAndUserId(challenge.getId(), requestingUserId)
                .stream()
                .map(a -> a.getMcq().getId())
                .collect(Collectors.toList());

        long opponentAnswered = challenge.getOpponent() != null
                ? challengeAttemptRepository.countByChallengeIdAndUserId(challenge.getId(), challenge.getOpponent().getId())
                : 0;
        long creatorAnswered = challengeAttemptRepository.countByChallengeIdAndUserId(challenge.getId(), challenge.getCreator().getId());
        long myAnswered = isMeCreator ? creatorAnswered : opponentAnswered;
        long theirAnswered = isMeCreator ? opponentAnswered : creatorAnswered;

        return ChallengeDetailResponse.builder()
                .id(challenge.getId())
                .mode(challenge.getMode().name())
                .status(challenge.getStatus().name())
                .creatorId(challenge.getCreator().getId())
                .creatorName(challenge.getCreator().getFullNameBn() != null ? challenge.getCreator().getFullNameBn() : challenge.getCreator().getFullName())
                .opponentId(challenge.getOpponent() != null ? challenge.getOpponent().getId() : null)
                .opponentName(challenge.getOpponent() != null
                        ? (challenge.getOpponent().getFullNameBn() != null ? challenge.getOpponent().getFullNameBn() : challenge.getOpponent().getFullName())
                        : null)
                .chapterId(challenge.getChapter() != null ? challenge.getChapter().getId() : null)
                .topicId(challenge.getTopic() != null ? challenge.getTopic().getId() : null)
                .questionCount(challenge.getQuestionCount())
                .createdAt(challenge.getCreatedAt())
                .expiresAt(challenge.getExpiresAt())
                .isMeCreator(isMeCreator)
                .hasIStarted(myAnswered > 0)
                .hasOpponentFinished(theirAnswered >= challenge.getQuestionCount())
                .answeredMcqIds(myAnsweredMcqIds)
                .myTurnDone(myAnswered >= challenge.getQuestionCount())
                .questions(questions)
                .build();
    }
        // ---------- বন্ধু খোঁজা (নাম/ফোন/ইমেইল দিয়ে সার্চ) ----------
    public List<UserSearchResultResponse> searchFriends(String currentUserId, String keyword) {
        if (keyword == null || keyword.trim().length() < 2) {
            return List.of();
        }
        return userRepository.searchUsers(keyword.trim()).stream()
                .filter(u -> !u.getId().equals(currentUserId))
                .limit(20)
                .map(u -> new UserSearchResultResponse(
                        u.getId(),
                        u.getFullName(),
                        u.getFullNameBn(),
                        u.getInstitutionName(),
                        u.getAvatarUrl()))
                .collect(Collectors.toList());
    }
}
