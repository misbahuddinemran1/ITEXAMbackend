package com.examplatform.modules.written.evaluation.manual.controller;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.manual.service.WrittenManualEvaluationService;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/evaluations/manual")
@RequiredArgsConstructor
public class AdminManualEvaluationController {

    private final WrittenManualEvaluationService manualEvaluationService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping("/{submissionId}")
    public EvaluationResponse submitManualEvaluation(
            @PathVariable String submissionId,
            @Valid @RequestBody ManualEvaluationRequest request,
            Authentication auth) {
        String adminId = resolveAdminId(auth);
        return manualEvaluationService.submitManualEvaluation(submissionId, request, adminId);
    }

    private String resolveAdminId(Authentication auth) {
        AdminUser adminUser = adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()));
        return adminUser.getId();
    }
}
