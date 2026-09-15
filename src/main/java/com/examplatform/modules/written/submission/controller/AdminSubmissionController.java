package com.examplatform.modules.written.submission.controller;

import com.examplatform.modules.written.submission.response.AdminSubmissionSummaryResponse;
import com.examplatform.modules.written.submission.service.AdminSubmissionQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/written/submissions")
@RequiredArgsConstructor
public class AdminSubmissionController {

    private final AdminSubmissionQueryService adminSubmissionQueryService;

    @GetMapping("/exam/{examId}")
    public List<AdminSubmissionSummaryResponse> getSubmissionsForExam(@PathVariable String examId) {
        return adminSubmissionQueryService.getSubmissionsForExam(examId);
    }

    @GetMapping("/{submissionId}")
    public AdminSubmissionSummaryResponse getSubmissionSummary(@PathVariable String submissionId) {
        return adminSubmissionQueryService.getSubmissionSummary(submissionId);
    }
}
