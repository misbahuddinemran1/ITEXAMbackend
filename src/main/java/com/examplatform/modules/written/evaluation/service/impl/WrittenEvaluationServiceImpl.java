package com.examplatform.modules.written.evaluation.service.impl;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.mapper.WrittenEvaluationMapper;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.response.WrittenLeaderboardEntryResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WrittenEvaluationServiceImpl implements WrittenEvaluationService {

    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository detailRepository;
    private final WrittenEvaluationMapper evaluationMapper;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenExamRepository examRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public EvaluationResponse getEvaluationBySubmissionId(String submissionId) {
        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found for submission: " + submissionId));
        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }

    @Override
    public EvaluationResponse getEvaluationById(String evaluationId) {
        WrittenEvaluation evaluation = getEvaluationOrThrow(evaluationId);
        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }

    @Override
    public List<EvaluationResponse> getEvaluationsForExam(String examId) {
        List<String> submissionIds = submissionRepository.findByExamId(examId).stream()
                .map(s -> s.getId())
                .toList();

        return evaluationRepository.findAll().stream()
                .filter(e -> submissionIds.contains(e.getSubmission().getId()))
                .map(e -> evaluationMapper.toResponse(e, detailRepository.findByEvaluationId(e.getId())))
                .toList();
    }

    /**
     * Written-exam leaderboard, mirroring the MCQ live-exam leaderboard semantics:
     * only original (non-practice) attempts, and only evaluations whose result has
     * been published to students. Cycle-agnostic — a student attempts a written exam
     * at most once in its lifetime (even across reopens/re-exams), so results from
     * every cycle are merged into one leaderboard. Ranked by totalMark descending.
     */
    @Override
    public List<WrittenLeaderboardEntryResponse> getLeaderboard(String examId, String requestingUserId) {
        WrittenExam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        List<WrittenEvaluation> evaluations = evaluationRepository
                .findLeaderboardByExamId(examId);

        BigDecimal totalMarks = BigDecimal.valueOf(exam.getTotalMarks());

        List<WrittenLeaderboardEntryResponse> result = new ArrayList<>();
        int rank = 1;
        for (WrittenEvaluation evaluation : evaluations) {
            String userId = evaluation.getSubmission().getUserId();
            Map<String, Object> userInfo = fetchUserNameAndCollege(userId);

            BigDecimal obtained = evaluation.getTotalMark() != null ? evaluation.getTotalMark() : BigDecimal.ZERO;
            double pct = totalMarks.doubleValue() > 0
                    ? obtained.doubleValue() / totalMarks.doubleValue() * 100
                    : 0;

            result.add(WrittenLeaderboardEntryResponse.builder()
                    .rank(rank++)
                    .userId(userId)
                    .userName((String) userInfo.getOrDefault("name", ""))
                    .collegeName((String) userInfo.getOrDefault("college", ""))
                    .obtainedMarks(obtained)
                    .totalMarks(totalMarks)
                    .percentage(BigDecimal.valueOf(pct).setScale(2, RoundingMode.HALF_UP).doubleValue())
                    .isCurrentUser(userId.equals(requestingUserId))
                    .build());
        }
        return result;
    }

    private Map<String, Object> fetchUserNameAndCollege(String userId) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT full_name, full_name_bn, institution_name FROM users WHERE id = ?", userId);
            if (rows.isEmpty()) return Map.of();
            Map<String, Object> row = rows.get(0);
            String fullNameBn = (String) row.get("full_name_bn");
            String fullName = (String) row.get("full_name");
            String name = (fullNameBn != null && !fullNameBn.isBlank()) ? fullNameBn : fullName;
            return Map.of("name", name == null ? "" : name, "college", row.getOrDefault("institution_name", ""));
        } catch (Exception e) {
            log.warn("Could not fetch user info for written leaderboard: {}", userId);
            return Map.of();
        }
    }

    private WrittenEvaluation getEvaluationOrThrow(String evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found: " + evaluationId));
    }
}
