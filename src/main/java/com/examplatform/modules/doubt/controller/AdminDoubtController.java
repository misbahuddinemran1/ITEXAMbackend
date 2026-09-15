package com.examplatform.modules.doubt.controller;

import com.examplatform.modules.doubt.dto.*;
import com.examplatform.modules.doubt.service.AdminDoubtService;
import com.examplatform.modules.doubt.service.DoubtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/doubts")
@RequiredArgsConstructor
public class AdminDoubtController {

    private final AdminDoubtService adminDoubtService;
    private final DoubtService doubtService;

    @GetMapping("/pending")
    public List<DoubtSummaryResponse> getPending() {
        return adminDoubtService.getByStatus("PENDING");
    }

    @GetMapping("/reviewed")
    public List<DoubtSummaryResponse> getReviewed() {
        return adminDoubtService.getByStatus("REVIEWED");
    }

    @GetMapping("/answered")
    public List<DoubtSummaryResponse> getAnswered() {
        return adminDoubtService.getByStatus("ANSWERED");
    }

    @GetMapping("/{id}")
    public DoubtResponse getDoubtDetail(@PathVariable String id) {
        return doubtService.getDoubtDetailForAdmin(id);
    }

    @PostMapping("/{id}/accept")
    public DoubtResponse accept(@PathVariable String id, Authentication auth) {
        return adminDoubtService.acceptDoubt(id, auth.getName());
    }

    @PostMapping("/{id}/generate-ai")
    public AiGenerateResponse generateAi(@PathVariable String id) {
        return adminDoubtService.generateAiPreview(id);
    }

    @PostMapping("/{id}/answer")
    public DoubtResponse saveAnswer(
            @PathVariable String id,
            @RequestBody AdminAnswerRequest request,
            Authentication auth) {
        return adminDoubtService.saveAnswer(id, auth.getName(), request);
    }
}
