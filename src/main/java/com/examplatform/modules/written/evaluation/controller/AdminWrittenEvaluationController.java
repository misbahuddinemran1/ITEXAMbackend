package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/evaluations")
@RequiredArgsConstructor
public class AdminWrittenEvaluationController {

    private final WrittenEvaluationService evaluationService;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenExamRepository examRepository;

    @GetMapping("/{evaluationId}")
    public EvaluationResponse getEvaluation(@PathVariable String evaluationId) {
        return evaluationService.getEvaluationById(evaluationId);
    }

    @GetMapping("/exam/{examId}")
    public List<EvaluationResponse> getEvaluationsForExam(@PathVariable String examId) {
        return evaluationService.getEvaluationsForExam(examId);
    }

    /**
     * Aggregate stats for an exam: how many submitted, how many graded, average/highest/lowest
     * mark, and a simple mark-range distribution — for a quick admin overview without having
     * to open every submission individually.
     */
    @GetMapping("/exam/{examId}/stats")
    public Map<String, Object> getExamStats(@PathVariable String examId) {
        WrittenExam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        int totalSubmissions = submissionRepository.findByExamId(examId).size();

        List<EvaluationResponse> evaluations = evaluationService.getEvaluationsForExam(examId);
        List<EvaluationResponse> completed = evaluations.stream()
                .filter(e -> "COMPLETED".equals(e.getStatus()) && e.getTotalMark() != null)
                .toList();

        BigDecimal totalMarksSum = completed.stream()
                .map(EvaluationResponse::getTotalMark)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal average = completed.isEmpty()
                ? BigDecimal.ZERO
                : totalMarksSum.divide(BigDecimal.valueOf(completed.size()), 2, RoundingMode.HALF_UP);

        BigDecimal highest = completed.stream().map(EvaluationResponse::getTotalMark)
                .max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal lowest = completed.stream().map(EvaluationResponse::getTotalMark)
                .min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);

        BigDecimal fullMark = BigDecimal.valueOf(exam.getTotalMarks() == null ? 0 : exam.getTotalMarks());
        BigDecimal passThreshold = fullMark.multiply(BigDecimal.valueOf(0.33)); // 33% pass line, common BD standard

        long passCount = completed.stream()
                .filter(e -> e.getTotalMark().compareTo(passThreshold) >= 0)
                .count();

        long publishedCount = evaluations.stream().filter(EvaluationResponse::isResultPublished).count();
        long pendingReviewCount = evaluations.stream().filter(e -> !"COMPLETED".equals(e.getStatus())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("examId", examId);
        stats.put("examTitle", exam.getTitle());
        stats.put("fullMark", fullMark);
        stats.put("totalSubmissions", totalSubmissions);
        stats.put("gradedCount", completed.size());
        stats.put("pendingReviewCount", pendingReviewCount);
        stats.put("publishedCount", publishedCount);
        stats.put("averageMark", average);
        stats.put("highestMark", highest);
        stats.put("lowestMark", lowest);
        stats.put("passCount", passCount);
        stats.put("passRate", completed.isEmpty() ? 0
                : BigDecimal.valueOf(passCount).multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(completed.size()), 1, RoundingMode.HALF_UP));

        return stats;
    }
}
