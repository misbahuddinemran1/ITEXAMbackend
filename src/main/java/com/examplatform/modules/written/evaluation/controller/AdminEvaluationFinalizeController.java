
package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationFinalizeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

/**
 * Finalizes an evaluation for ANY evaluation mode (MANUAL, AI, or HYBRID).
 * Admin reviews predicted marks (if any) shown in GET /written/evaluations/submission/{id},
 * edits them as needed, and submits the final per-part marks here.
 */
@RestController
@RequestMapping("/admin/written/evaluations/finalize")
@RequiredArgsConstructor
public class AdminEvaluationFinalizeController {

    private final WrittenEvaluationFinalizeService finalizeService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping("/{submissionId}")
    public EvaluationResponse finalizeEvaluation(
            @PathVariable String submissionId,
            @Valid @RequestBody ManualEvaluationRequest request,
            Authentication auth) {
        String adminId = resolveAdminId(auth);
        return finalizeService.finalizeEvaluation(submissionId, request, adminId);
    }

    /**
     * For exams where written_settings.resultPublishMode = MANUAL, finalizing an evaluation
     * does NOT reveal the mark to the student automatically. The admin calls this endpoint
     * whenever they're ready to publish that specific student's result.
     */
    @PostMapping("/{submissionId}/publish-result")
    public EvaluationResponse publishResult(@PathVariable String submissionId) {
        return finalizeService.publishResult(submissionId);
    }

    /**
     * Publishes all COMPLETED-but-unpublished evaluations for an exam at once.
     * Returns how many results were actually published (already-published ones are skipped).
     */
    @PostMapping("/exam/{examId}/publish-all-results")
    public java.util.Map<String, Object> publishAllResults(@PathVariable String examId) {
        int count = finalizeService.publishAllResultsForExam(examId);
        return java.util.Map.of(
                "examId", examId,
                "publishedCount", count,
                "message", "Published " + count + " result(s) for exam " + examId
        );
    }

    private String resolveAdminId(Authentication auth) {
        return adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()))
                .getId();
    }
}
