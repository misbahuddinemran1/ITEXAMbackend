package com.examplatform.modules.written.evaluation.controller;

import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.evaluation.service.EvaluationOrchestrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/evaluations")
@RequiredArgsConstructor
public class AdminEvaluationOrchestrationController {

    private final EvaluationOrchestrationService evaluationOrchestrationService;
    private final AdminUserRepository adminUserRepository;

    /**
     * Admin triggers predicted-mark matching for a submission.
     * Requires transcription to already be done for all AI-mode parts.
     */
    @PostMapping("/{submissionId}/predict-marks")
    public String predictMarks(@PathVariable String submissionId, Authentication auth) {
        String adminId = resolveAdminId(auth);
        evaluationOrchestrationService.runPredictedMatching(submissionId, adminId);
        return "Predicted marks calculated for submission: " + submissionId;
    }

    private String resolveAdminId(Authentication auth) {
        return adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()))
                .getId();
    }
}
