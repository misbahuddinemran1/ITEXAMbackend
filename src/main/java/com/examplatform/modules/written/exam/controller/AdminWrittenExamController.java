package com.examplatform.modules.written.exam.controller;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.written.exam.request.CreateExamRequest;
import com.examplatform.modules.written.exam.request.ReopenExamRequest;
import com.examplatform.modules.written.exam.request.UpdateExamRequest;
import com.examplatform.modules.written.exam.response.ExamResponse;
import com.examplatform.modules.written.exam.service.WrittenExamService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/exams")
@RequiredArgsConstructor
public class AdminWrittenExamController {

    private final WrittenExamService examService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping
    public ExamResponse createExam(@RequestBody CreateExamRequest request, Authentication auth) {
        String adminId = resolveAdminId(auth);
        return examService.createExam(request, adminId);
    }

    @PutMapping("/{examId}")
    public ExamResponse updateExam(@PathVariable String examId, @RequestBody UpdateExamRequest request) {
        return examService.updateExam(examId, request);
    }

    @PatchMapping("/{examId}/publish")
    public ExamResponse publishExam(@PathVariable String examId) {
        return examService.publishExam(examId);
    }

    @PatchMapping("/{examId}/go-live")
    public ExamResponse goLive(@PathVariable String examId) {
        return examService.goLive(examId);
    }

    @PatchMapping("/{examId}/end")
    public ExamResponse endExam(@PathVariable String examId) {
        return examService.endExam(examId);
    }

    @PatchMapping("/{examId}/archive")
    public ExamResponse archiveExam(@PathVariable String examId) {
        return examService.archiveExam(examId);
    }

    @PatchMapping("/{examId}/reopen")
    public ExamResponse reopenExam(@PathVariable String examId, @RequestBody ReopenExamRequest request) {
        return examService.reopenExam(examId, request);
    }

    @GetMapping
    public List<ExamResponse> getAllExamsForAdmin() {
        return examService.getAllExamsForAdmin();
    }

    @GetMapping("/{examId}")
    public ExamResponse getExamById(@PathVariable String examId) {
        return examService.getExamById(examId);
    }

    @DeleteMapping("/{examId}")
    public void deleteExam(@PathVariable String examId) {
        examService.deleteExam(examId);
    }

    private String resolveAdminId(Authentication auth) {
        AdminUser adminUser = adminUserRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new NoSuchElementException("Admin user not found: " + auth.getName()));
        return adminUser.getId();
    }
}
