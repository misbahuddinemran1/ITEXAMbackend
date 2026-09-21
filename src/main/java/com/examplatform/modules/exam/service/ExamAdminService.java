package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.request.*;
import com.examplatform.modules.exam.dto.response.*;
import com.examplatform.modules.exam.entity.*;
import com.examplatform.modules.exam.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamAdminService {

    private final ExamRepository examRepository;
    private final ExamSubjectConfigRepository subjectConfigRepository;
    private final ExamTopicConfigRepository topicConfigRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAttemptHistoryRepository attemptHistoryRepository;
    private final JdbcTemplate jdbcTemplate;

@Transactional
public ExamResponse extendExam(String examId, LocalDate newExamDate,
                                LocalTime newStartTime, LocalTime newEndTime) {
    Exam exam = findExamById(examId);

    exam.setExamDate(newExamDate);
    exam.setStartTime(newStartTime);
    exam.setEndTime(newEndTime);
    exam.setCycleNumber(exam.getCycleNumber() + 1);
    exam.setPublishStatus(Exam.PublishStatus.PUBLISHED);

    Exam extended = examRepository.save(exam);
    log.info("Exam extended: {} -> cycle {}", examId, extended.getCycleNumber());
    return buildExamResponse(extended);
}
    
    // ============================================
    // CREATE EXAM
    // ============================================
    @Transactional
    public ExamResponse createExam(ExamCreationRequest request, String adminId) {
        log.info("Creating exam: {} by admin: {}", request.getName(), adminId);

        // 1. Exam entity তৈরি
        Exam exam = Exam.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .examCode(generateExamCode(request))
                .examType(Exam.ExamType.valueOf(request.getExamType()))
                .publishStatus(Exam.PublishStatus.DRAFT)
                .description(request.getDescription())
                .totalMarks(BigDecimal.valueOf(request.getTotalMarks()))
                .passMarks(BigDecimal.valueOf(request.getPassMarks()))
                .negativeMarking(BigDecimal.valueOf(request.getNegativeMarking()))
                .durationMinutes(request.getDurationMinutes())
                .examDate(request.getExamDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .maxAttempts(request.getMaxAttempts())
                .allowReview(request.isAllowReview())
                .shuffleQuestions(request.isShuffleQuestions())
                .shuffleOptions(request.isShuffleOptions())
                .showResultAfterSubmit(request.isShowResultAfterSubmit())
                .isPremiumOnly(request.isPremiumOnly())
                .targetLevels(resolveTargetLevels(request.getTargetLevels()))
                .createdBy(adminId)
                .build();

        Exam savedExam = examRepository.save(exam);

        // 2. Subject Config save
        if (request.getSubjectConfigs() != null
                && !request.getSubjectConfigs().isEmpty()) {
            saveSubjectConfigs(savedExam.getId(), request.getSubjectConfigs());
        }

        // 3. Topic Config save
        if (request.getTopicConfigs() != null
                && !request.getTopicConfigs().isEmpty()) {
            saveTopicConfigs(savedExam.getId(), request.getTopicConfigs());
        }

        // 4. Manual questions add (admin নিজে select করলে)
        if (request.getManualQuestionIds() != null
                && !request.getManualQuestionIds().isEmpty()) {
            addManualQuestions(
                    savedExam.getId(),
                    request.getManualQuestionIds(),
                    request.getTotalMarks() / request.getManualQuestionIds().size()
            );
        } else {
            // 5. Auto question select (config অনুযায়ী)
            autoSelectQuestions(savedExam);
        }

        // 6. Total question count update
        long questionCount = examQuestionRepository.countByExamId(savedExam.getId());
        savedExam.setTotalQuestions((int) questionCount);
        examRepository.save(savedExam);

        log.info("Exam created successfully: {}", savedExam.getId());
        return buildExamResponse(savedExam);
    }

    // ============================================
    // UPDATE EXAM (শুধু DRAFT exam update হবে)
    // ============================================

    @Transactional
    public ExamResponse updateExam(String examId, ExamCreationRequest request) {
        Exam exam = findExamById(examId);

        if (Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            throw new RuntimeException(
                    "Published exam সরাসরি এডিট করা যাবে না। Archive করে তারপর এডিট করুন, অথবা Extend ব্যবহার করুন।"
            );
        }
        // Basic info update
        exam.setName(request.getName());
        exam.setDescription(request.getDescription());
        exam.setExamType(Exam.ExamType.valueOf(request.getExamType()));
        exam.setTotalMarks(BigDecimal.valueOf(request.getTotalMarks()));
        exam.setPassMarks(BigDecimal.valueOf(request.getPassMarks()));
        exam.setNegativeMarking(BigDecimal.valueOf(request.getNegativeMarking()));
        exam.setDurationMinutes(request.getDurationMinutes());
        exam.setExamDate(request.getExamDate());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setMaxAttempts(request.getMaxAttempts());
        exam.setAllowReview(request.isAllowReview());
        exam.setShuffleQuestions(request.isShuffleQuestions());
        exam.setShuffleOptions(request.isShuffleOptions());
        exam.setShowResultAfterSubmit(request.isShowResultAfterSubmit());
        exam.setPremiumOnly(request.isPremiumOnly());
        exam.setTargetLevels(resolveTargetLevels(request.getTargetLevels()));

        // পুরানো configs মুছে নতুন save
        subjectConfigRepository.deleteByExamId(examId);
        topicConfigRepository.deleteByExamId(examId);
        examQuestionRepository.deleteByExamId(examId);

        if (request.getSubjectConfigs() != null
                && !request.getSubjectConfigs().isEmpty()) {
            saveSubjectConfigs(examId, request.getSubjectConfigs());
        }

        if (request.getTopicConfigs() != null
                && !request.getTopicConfigs().isEmpty()) {
            saveTopicConfigs(examId, request.getTopicConfigs());
        }

        if (request.getManualQuestionIds() != null
                && !request.getManualQuestionIds().isEmpty()) {
            addManualQuestions(
                    examId,
                    request.getManualQuestionIds(),
                    request.getTotalMarks() / request.getManualQuestionIds().size()
            );
        } else {
            autoSelectQuestions(exam);
        }

        long questionCount = examQuestionRepository.countByExamId(examId);
        exam.setTotalQuestions((int) questionCount);

        Exam updated = examRepository.save(exam);
        return buildExamResponse(updated);
    }

    // ============================================
    // PUBLISH EXAM
    // ============================================
    @Transactional
    public ExamResponse publishExam(String examId) {
        Exam exam = findExamById(examId);

        if (Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            throw new RuntimeException("Exam is already published.");
        }

        long questionCount = examQuestionRepository.countByExamId(examId);
        if (questionCount == 0) {
            throw new RuntimeException(
                    "Cannot publish exam with no questions. " +
                            "Please add questions first."
            );
        }

        exam.setPublishStatus(Exam.PublishStatus.PUBLISHED);
        Exam published = examRepository.save(exam);

        log.info("Exam published/republished: {}", examId);
        return buildExamResponse(published);
    }
    // ============================================
    // ARCHIVE EXAM
    // ============================================
    @Transactional
    public ExamResponse archiveExam(String examId) {
        Exam exam = findExamById(examId);
        exam.setPublishStatus(Exam.PublishStatus.ARCHIVED);
        Exam archived = examRepository.save(exam);
        return buildExamResponse(archived);
    }

    // ============================================
    // DELETE EXAM (শুধু DRAFT delete হবে)
    // ============================================
    @Transactional
    public void deleteExam(String examId) {
        Exam exam = findExamById(examId);

        if (!Exam.PublishStatus.DRAFT.equals(exam.getPublishStatus())) {
            throw new RuntimeException(
                    "Only DRAFT exams can be deleted. " +
                            "Archive the exam instead."
            );
        }

        subjectConfigRepository.deleteByExamId(examId);
        topicConfigRepository.deleteByExamId(examId);
        examQuestionRepository.deleteByExamId(examId);
        examRepository.delete(exam);

        log.info("Exam deleted: {}", examId);
    }

    // ============================================
    // ADD QUESTIONS MANUALLY
    // ============================================
    @Transactional
    public void addQuestionsManually(String examId, AddQuestionsRequest request) {
        Exam exam = findExamById(examId);

        if (Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            throw new RuntimeException(
                    "Cannot add questions to a published exam."
            );
        }

        addManualQuestions(
                examId,
                request.getQuestionIds(),
                request.getMarksPerQuestion()
        );

        long questionCount = examQuestionRepository.countByExamId(examId);
        exam.setTotalQuestions((int) questionCount);
        examRepository.save(exam);
    }

    // ============================================
    // REGENERATE QUESTIONS (auto-select আবার করো)
    // ============================================
    @Transactional
    public ExamResponse regenerateQuestions(String examId) {
        Exam exam = findExamById(examId);

        if (Exam.PublishStatus.PUBLISHED.equals(exam.getPublishStatus())) {
            throw new RuntimeException(
                    "Cannot regenerate questions for a published exam."
            );
        }

        examQuestionRepository.deleteByExamId(examId);
        autoSelectQuestions(exam);

        long questionCount = examQuestionRepository.countByExamId(examId);
        exam.setTotalQuestions((int) questionCount);
        Exam updated = examRepository.save(exam);

        return buildExamResponse(updated);
    }

    // ============================================
    // GET ALL EXAMS (Admin List)
    // ============================================
    public List<ExamListResponse> getAllExams() {
        return examRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::buildExamListResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // GET EXAMS BY TYPE
    // ============================================
    public List<ExamListResponse> getExamsByType(String examType) {
        return examRepository
                .findByExamTypeOrderByCreatedAtDesc(Exam.ExamType.valueOf(examType))
                .stream()
                .map(this::buildExamListResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // GET EXAM DETAIL
    // ============================================
    public ExamResponse getExamDetail(String examId) {
        Exam exam = findExamById(examId);
        return buildExamResponse(exam);
    }

    // ============================================
    // ADMIN STATS
    // ============================================
    public ExamStatsResponse getExamStats() {
        return ExamStatsResponse.builder()
                .totalExams(examRepository.count())
                .publishedExams(examRepository
                        .countByPublishStatus(Exam.PublishStatus.PUBLISHED))
                .draftExams(examRepository
                        .countByPublishStatus(Exam.PublishStatus.DRAFT))
                .archivedExams(examRepository
                        .countByPublishStatus(Exam.PublishStatus.ARCHIVED))
                .dailyExams(examRepository
                        .countByExamType(Exam.ExamType.DAILY))
                .weeklyExams(examRepository
                        .countByExamType(Exam.ExamType.WEEKLY))
                .revisionExams(examRepository
                        .countByExamType(Exam.ExamType.REVISION))
                .subjectWiseExams(examRepository
                        .countByExamType(Exam.ExamType.SUBJECT_WISE))
                .mixedExams(examRepository
                        .countByExamType(Exam.ExamType.MIXED))
                .totalAttempts(attemptHistoryRepository.count())
                .build();
    }

    // ============================================
    // GET EXAM QUESTIONS (with details)
    // ============================================
    public List<ExamQuestionDetailResponse> getExamQuestions(String examId) {
        List<ExamQuestion> examQuestions = examQuestionRepository
                .findByExamIdOrderByOrderNumberAsc(examId);

        return examQuestions.stream()
                .map(this::buildExamQuestionDetailResponse)
                .collect(Collectors.toList());
    }

    // ============================================
    // REPLACE INDIVIDUAL QUESTION
    // ============================================
    @Transactional
    public void replaceExamQuestion(String examId, String examQuestionId, String newQuestionId) {
        ExamQuestion existing = examQuestionRepository.findById(examQuestionId)
                .orElseThrow(() -> new RuntimeException("Exam question not found: " + examQuestionId));

        if (!existing.getExamId().equals(examId)) {
            throw new RuntimeException("এই প্রশ্নটি এই exam-এর অংশ না");
        }

        if (examQuestionRepository.existsByExamIdAndQuestionId(examId, newQuestionId)) {
            throw new RuntimeException("এই প্রশ্নটি ইতিমধ্যে exam-এ আছে");
        }

        existing.setQuestionId(newQuestionId);
        examQuestionRepository.save(existing);

        log.info("Exam {}: question {} replaced with {}", examId, examQuestionId, newQuestionId);
    }



    // ============================================
    // PRIVATE — AUTO SELECT QUESTIONS
    // Config অনুযায়ী question bank থেকে select
    // ============================================
    private void autoSelectQuestions(Exam exam) {
        String examId = exam.getId();
        List<String> selectedQuestionIds = new ArrayList<>();

        // Subject level config আছে?
        List<ExamSubjectConfig> subjectConfigs =
                subjectConfigRepository.findByExamId(examId);

        if (!subjectConfigs.isEmpty()) {
            for (ExamSubjectConfig config : subjectConfigs) {
                List<String> ids = selectQuestionsBySubjectConfig(
                        config, selectedQuestionIds
                );
                selectedQuestionIds.addAll(ids);
            }
        }

        // Topic level config আছে?
        List<ExamTopicConfig> topicConfigs =
                topicConfigRepository.findByExamId(examId);

        if (!topicConfigs.isEmpty()) {
            for (ExamTopicConfig config : topicConfigs) {
                List<String> ids = selectQuestionsByTopicConfig(
                        config, selectedQuestionIds
                );
                selectedQuestionIds.addAll(ids);
            }
        }

        // ExamQuestion হিসেবে save
        if (!selectedQuestionIds.isEmpty()) {
            double marksPerQuestion = exam.getTotalMarks()
                    .divide(BigDecimal.valueOf(selectedQuestionIds.size()),
                            2, RoundingMode.HALF_UP)
                    .doubleValue();

            for (int i = 0; i < selectedQuestionIds.size(); i++) {
                String qId = selectedQuestionIds.get(i);
                if (!examQuestionRepository.existsByExamIdAndQuestionId(examId, qId)) {
                    ExamQuestion eq = ExamQuestion.builder()
                            .id(UUID.randomUUID().toString())
                            .examId(examId)
                            .questionId(qId)
                            .marks(BigDecimal.valueOf(marksPerQuestion))
                            .orderNumber(i + 1)
                            .build();
                    examQuestionRepository.save(eq);
                }
            }
        }

        log.info("Auto-selected {} questions for exam: {}",
                selectedQuestionIds.size(), examId);
    }

    // Subject Config অনুযায়ী questions select
    private List<String> selectQuestionsBySubjectConfig(
            ExamSubjectConfig config,
            List<String> alreadySelected) {

        List<String> result = new ArrayList<>();
        int total = config.getQuestionCount();

        // Difficulty distribution
        int easyCount   = calculateCount(total, config.getEasyPercent());
        int mediumCount = calculateCount(total, config.getMediumPercent());
        int hardCount   = total - easyCount - mediumCount;

        // Easy questions
        if (easyCount > 0) {
            result.addAll(fetchQuestionsBySubjectAndDifficulty(
                    config.getSubjectId(), 1, easyCount,
                    config, alreadySelected, result
            ));
        }

        // Medium questions
        if (mediumCount > 0) {
            result.addAll(fetchQuestionsBySubjectAndDifficulty(
                    config.getSubjectId(), 2, mediumCount,
                    config, alreadySelected, result
            ));
        }

        // Hard questions
        if (hardCount > 0) {
            result.addAll(fetchQuestionsBySubjectAndDifficulty(
                    config.getSubjectId(), 3, hardCount,
                    config, alreadySelected, result
            ));
        }

        return result;
    }

    // Topic Config অনুযায়ী questions select
    private List<String> selectQuestionsByTopicConfig(
            ExamTopicConfig config,
            List<String> alreadySelected) {

        List<String> result = new ArrayList<>();
        int total = config.getQuestionCount();

        int easyCount   = calculateCount(total, config.getEasyPercent());
        int mediumCount = calculateCount(total, config.getMediumPercent());
        int hardCount   = total - easyCount - mediumCount;

        if (easyCount > 0) {
            result.addAll(fetchQuestionsByTopicAndDifficulty(
                    config, 1, easyCount, alreadySelected, result
            ));
        }
        if (mediumCount > 0) {
            result.addAll(fetchQuestionsByTopicAndDifficulty(
                    config, 2, mediumCount, alreadySelected, result
            ));
        }
        if (hardCount > 0) {
            result.addAll(fetchQuestionsByTopicAndDifficulty(
                    config, 3, hardCount, alreadySelected, result
            ));
        }

        return result;
    }



    // Subject + Difficulty + Cognitive level অনুযায়ী fetch
    private List<String> fetchQuestionsBySubjectAndDifficulty(
            String subjectId,
            int difficultyLevel,
            int count,
            ExamSubjectConfig config,
            List<String> globalExclude,
            List<String> localExclude) {

        List<String> result = new ArrayList<>();
        List<String> exclude = new ArrayList<>(globalExclude);
        exclude.addAll(localExclude);

        // Cognitive level অনুযায়ী ভাগ করো
        Map<String, Integer> cognitiveCounts =
                buildCognitiveCounts(count, config);

        for (Map.Entry<String, Integer> entry : cognitiveCounts.entrySet()) {
            if (entry.getValue() <= 0) continue;

            String inClause = buildInClause(exclude);
            String sql = "SELECT q.id FROM questions q " +
                    "WHERE q.status = 'APPROVED' " +
                    "AND q.subject_id = ? " +
                    "AND q.difficulty_level = ? " +
                    "AND q.cognitive_level = ? " +
                    (inClause.isEmpty() ? "" : "AND q.id NOT IN (" + inClause + ") ") +
                    "ORDER BY RANDOM() LIMIT ?";

            List<String> ids = jdbcTemplate.queryForList(
                    sql, String.class,
                    subjectId, difficultyLevel,
                    entry.getKey(), entry.getValue()
            );

            result.addAll(ids);
            exclude.addAll(ids);
        }

        // Cognitive পূরণ না হলে — যেকোনো cognitive থেকে নাও
        int remaining = count - result.size();
        if (remaining > 0) {
            String inClause = buildInClause(exclude);
            String fallbackSql = "SELECT q.id FROM questions q " +
                    "WHERE q.status = 'APPROVED' " +
                    "AND q.subject_id = ? " +
                    "AND q.difficulty_level = ? " +
                    (inClause.isEmpty() ? "" : "AND q.id NOT IN (" + inClause + ") ") +
                    "ORDER BY RANDOM() LIMIT ?";

            List<String> fallback = jdbcTemplate.queryForList(
                    fallbackSql, String.class,
                    subjectId, difficultyLevel, remaining
            );
            result.addAll(fallback);
        }

        return result;
    }

    // Topic + Difficulty অনুযায়ী fetch
    private List<String> fetchQuestionsByTopicAndDifficulty(
            ExamTopicConfig config,
            int difficultyLevel,
            int count,
            List<String> globalExclude,
            List<String> localExclude) {

        List<String> exclude = new ArrayList<>(globalExclude);
        exclude.addAll(localExclude);
        String inClause = buildInClause(exclude);

        // Topic → Chapter → Subject (ক্রম অনুযায়ী specific থেকে broad)
        String whereClause;
        Object[] params;

        if (config.getTopicId() != null) {
            whereClause = "AND q.topic_id = ? ";
            params = new Object[]{
                    difficultyLevel, config.getTopicId(), count
            };
        } else if (config.getChapterId() != null) {
            whereClause = "AND q.chapter_id = ? ";
            params = new Object[]{
                    difficultyLevel, config.getChapterId(), count
            };
        } else {
            whereClause = "AND q.subject_id = ? ";
            params = new Object[]{
                    difficultyLevel, config.getSubjectId(), count
            };
        }

        String sql = "SELECT q.id FROM questions q " +
                "WHERE q.status = 'APPROVED' " +
                "AND q.difficulty_level = ? " +
                whereClause +
                (inClause.isEmpty() ? "" : "AND q.id NOT IN (" + inClause + ") ") +
                "ORDER BY RANDOM() LIMIT ?";

        return jdbcTemplate.queryForList(sql, String.class, params);
    }

    // ============================================
    // PRIVATE — HELPER METHODS
    // ============================================

    private void saveSubjectConfigs(String examId,
                                    List<ExamSubjectConfigRequest> requests) {
        for (ExamSubjectConfigRequest req : requests) {
            ExamSubjectConfig config = ExamSubjectConfig.builder()
                    .id(UUID.randomUUID().toString())
                    .examId(examId)
                    .subjectId(req.getSubjectId())
                    .questionCount(req.getQuestionCount())
                    .marksPerQuestion(BigDecimal.valueOf(req.getMarksPerQuestion()))
                    .easyPercent(req.getEasyPercent())
                    .mediumPercent(req.getMediumPercent())
                    .hardPercent(req.getHardPercent())
                    .rememberPercent(req.getRememberPercent())
                    .understandPercent(req.getUnderstandPercent())
                    .applyPercent(req.getApplyPercent())
                    .analyzePercent(req.getAnalyzePercent())
                    .evaluatePercent(req.getEvaluatePercent())
                    .build();
            subjectConfigRepository.save(config);
        }
    }

    private void saveTopicConfigs(String examId,
                                  List<ExamTopicConfigRequest> requests) {
        for (ExamTopicConfigRequest req : requests) {
            ExamTopicConfig config = ExamTopicConfig.builder()
                    .id(UUID.randomUUID().toString())
                    .examId(examId)
                    .subjectId(req.getSubjectId())
                    .chapterId(req.getChapterId())
                    .topicId(req.getTopicId())
                    .questionCount(req.getQuestionCount())
                    .marksPerQuestion(BigDecimal.valueOf(req.getMarksPerQuestion()))
                    .easyPercent(req.getEasyPercent())
                    .mediumPercent(req.getMediumPercent())
                    .hardPercent(req.getHardPercent())
                    .rememberPercent(req.getRememberPercent())
                    .understandPercent(req.getUnderstandPercent())
                    .applyPercent(req.getApplyPercent())
                    .analyzePercent(req.getAnalyzePercent())
                    .evaluatePercent(req.getEvaluatePercent())
                    .build();
            topicConfigRepository.save(config);
        }
    }

    private void addManualQuestions(String examId,
                                    List<String> questionIds,
                                    double marksPerQuestion) {
        for (int i = 0; i < questionIds.size(); i++) {
            String qId = questionIds.get(i);
            if (!examQuestionRepository.existsByExamIdAndQuestionId(examId, qId)) {
                ExamQuestion eq = ExamQuestion.builder()
                        .id(UUID.randomUUID().toString())
                        .examId(examId)
                        .questionId(qId)
                        .marks(BigDecimal.valueOf(marksPerQuestion))
                        .orderNumber(i + 1)
                        .build();
                examQuestionRepository.save(eq);
            }
        }
    }

    // Cognitive distribution calculate করো
    private Map<String, Integer> buildCognitiveCounts(
            int total, ExamSubjectConfig config) {

        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("REMEMBER",   calculateCount(total, config.getRememberPercent()));
        map.put("UNDERSTAND", calculateCount(total, config.getUnderstandPercent()));
        map.put("APPLY",      calculateCount(total, config.getApplyPercent()));
        map.put("ANALYZE",    calculateCount(total, config.getAnalyzePercent()));
        map.put("EVALUATE",   calculateCount(total, config.getEvaluatePercent()));
        return map;
    }

    // % থেকে count calculate
    private int calculateCount(int total, int percent) {
        if (percent <= 0) return 0;
        return (int) Math.round(total * percent / 100.0);
    }

    // SQL NOT IN clause build
    private String buildInClause(List<String> ids) {
        if (ids == null || ids.isEmpty()) return "";
        return ids.stream()
                .map(id -> "'" + id + "'")
                .collect(Collectors.joining(","));
    }

    // targetLevels null/empty হলে default ["ALL"]
    private List<String> resolveTargetLevels(List<String> targetLevels) {
        return (targetLevels == null || targetLevels.isEmpty())
                ? List.of("ALL")
                : targetLevels;
    }

    // Exam code generate
    private String generateExamCode(ExamCreationRequest request) {
        String prefix = request.getExamType().substring(0, 3).toUpperCase();
        String date = request.getExamDate() != null
                ? request.getExamDate().toString().replace("-", "").substring(2)
                : LocalDate.now().toString().replace("-", "").substring(2);
        String random = String.valueOf((int)(Math.random() * 900) + 100);
        String code = prefix + "-" + date + "-" + random;

        // Duplicate check
        while (examRepository.existsByExamCode(code)) {
            random = String.valueOf((int)(Math.random() * 900) + 100);
            code = prefix + "-" + date + "-" + random;
        }
        return code;
    }

    private Exam findExamById(String examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException(
                        "Exam not found with id: " + examId
                ));
    }

    // ============================================
    // RESPONSE BUILDERS
    // ============================================
    private ExamResponse buildExamResponse(Exam exam) {
        List<ExamSubjectConfig> subjectConfigs =
                subjectConfigRepository.findByExamId(exam.getId());
        List<ExamTopicConfig> topicConfigs =
                topicConfigRepository.findByExamId(exam.getId());

        long totalAttempts = attemptHistoryRepository
                .findByExamIdOrderByPercentageDesc(exam.getId()).size();

        return ExamResponse.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examCode(exam.getExamCode())
                .examType(exam.getExamType().name())
                .publishStatus(exam.getPublishStatus().name())
                .description(exam.getDescription())
                .totalQuestions(exam.getTotalQuestions())
                .totalMarks(exam.getTotalMarks().doubleValue())
                .passMarks(exam.getPassMarks().doubleValue())
                .negativeMarking(exam.getNegativeMarking().doubleValue())
                .durationMinutes(exam.getDurationMinutes())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .maxAttempts(exam.getMaxAttempts())
                .attemptsAllowed(exam.getMaxAttempts() == null
                        ? "Unlimited"
                        : String.valueOf(exam.getMaxAttempts()))
                .allowReview(exam.isAllowReview())
                .shuffleQuestions(exam.isShuffleQuestions())
                .shuffleOptions(exam.isShuffleOptions())
                .showResultAfterSubmit(exam.isShowResultAfterSubmit())
                .isPremiumOnly(exam.isPremiumOnly())
                .subjectConfigs(subjectConfigs.stream()
                        .map(this::buildSubjectConfigResponse)
                        .collect(Collectors.toList()))
                .topicConfigs(topicConfigs.stream()
                        .map(this::buildTopicConfigResponse)
                        .collect(Collectors.toList()))
                .totalAttempts(totalAttempts)
                .createdBy(exam.getCreatedBy())
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .build();
    }

    private ExamListResponse buildExamListResponse(Exam exam) {
        long attempts = attemptHistoryRepository
                .findByExamIdOrderByPercentageDesc(exam.getId()).size();

        return ExamListResponse.builder()
                .id(exam.getId())
                .name(exam.getName())
                .examCode(exam.getExamCode())
                .examType(exam.getExamType().name())
                .publishStatus(exam.getPublishStatus().name())
                .totalQuestions(exam.getTotalQuestions())
                .totalMarks(exam.getTotalMarks().doubleValue())
                .durationMinutes(exam.getDurationMinutes())
                .examDate(exam.getExamDate())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .attemptsAllowed(exam.getMaxAttempts() == null
                        ? "Unlimited"
                        : String.valueOf(exam.getMaxAttempts()))
                .isPremiumOnly(exam.isPremiumOnly())
                .totalAttempts(attempts)
                .targetLevels(exam.getTargetLevels())
                .build();
    }

    private ExamSubjectConfigResponse buildSubjectConfigResponse(
            ExamSubjectConfig config) {

        // Subject name fetch
        String subjectName = "";
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT name FROM subjects WHERE id = ?",
                    config.getSubjectId()
            );
            if (!result.isEmpty()) {
                subjectName = (String) result.get(0).get("name");
            }
        } catch (Exception e) {
            log.warn("Could not fetch subject name: {}", config.getSubjectId());
        }

        return ExamSubjectConfigResponse.builder()
                .id(config.getId())
                .subjectId(config.getSubjectId())
                .subjectName(subjectName)
                .questionCount(config.getQuestionCount())
                .marksPerQuestion(config.getMarksPerQuestion().doubleValue())
                .easyPercent(config.getEasyPercent())
                .mediumPercent(config.getMediumPercent())
                .hardPercent(config.getHardPercent())
                .rememberPercent(config.getRememberPercent())
                .understandPercent(config.getUnderstandPercent())
                .applyPercent(config.getApplyPercent())
                .analyzePercent(config.getAnalyzePercent())
                .evaluatePercent(config.getEvaluatePercent())
                .build();
    }

    private ExamTopicConfigResponse buildTopicConfigResponse(
            ExamTopicConfig config) {

        return ExamTopicConfigResponse.builder()
                .id(config.getId())
                .subjectId(config.getSubjectId())
                .chapterId(config.getChapterId())
                .topicId(config.getTopicId())
                .questionCount(config.getQuestionCount())
                .marksPerQuestion(config.getMarksPerQuestion().doubleValue())
                .easyPercent(config.getEasyPercent())
                .mediumPercent(config.getMediumPercent())
                .hardPercent(config.getHardPercent())
                .rememberPercent(config.getRememberPercent())
                .understandPercent(config.getUnderstandPercent())
                .applyPercent(config.getApplyPercent())
                .analyzePercent(config.getAnalyzePercent())
                .evaluatePercent(config.getEvaluatePercent())
                .build();
    }

    // ============================================
    // NEW — Exam Question Detail Builder
    // ============================================
    private ExamQuestionDetailResponse buildExamQuestionDetailResponse(ExamQuestion eq) {
        Map<String, Object> q = jdbcTemplate.queryForList(
                "SELECT question_text, difficulty_level, subject_id FROM questions WHERE id = ?",
                eq.getQuestionId()
        ).stream().findFirst().orElse(Map.of());

        return ExamQuestionDetailResponse.builder()
                .examQuestionId(eq.getId())
                .questionId(eq.getQuestionId())
                .questionText((String) q.get("question_text"))
                .difficultyLevel((Integer) q.get("difficulty_level"))
                .marks(eq.getMarks().doubleValue())
                .orderNumber(eq.getOrderNumber())
                .build();
    }


    
}
