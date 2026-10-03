package com.examplatform.modules.exam.controller;

import com.examplatform.modules.exam.dto.response.*;
import com.examplatform.modules.exam.service.ExamStudentService;
import lombok.RequiredArgsConstructor;
import com.examplatform.infrastructure.security.CurrentUserId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamStudentController {

    private final ExamStudentService examStudentService;

    // ============================================
    // GET ALL AVAILABLE EXAMS
    // GET /api/v1/exams/available
    // ============================================
    @GetMapping("/available")
    public ResponseEntity<?> getAvailableExams(
            @CurrentUserId String userId) {
        try {
            List<AvailableExamResponse> exams =
                    examStudentService.getAvailableExams(userId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", exams,
                            "total", exams.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching available exams: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET AVAILABLE EXAMS BY TYPE
    // GET /api/v1/exams/available/type/DAILY
    // ============================================
    @GetMapping("/available/type/{examType}")
    public ResponseEntity<?> getAvailableExamsByType(
            @CurrentUserId String userId,
            @PathVariable String examType) {
        try {
            List<AvailableExamResponse> exams =
                    examStudentService.getAvailableExamsByType(userId, examType);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", exams,
                            "total", exams.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching exams by type {}: {}",
                    examType, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET EXAM DETAIL (Student)
    // GET /api/v1/exams/{examId}
    // ============================================
    @GetMapping("/{examId}")
    public ResponseEntity<?> getExamDetail(
            @PathVariable String examId,
            @CurrentUserId String userId) {
        try {
            AvailableExamResponse response =
                    examStudentService.getExamDetail(examId, userId);
            return ResponseEntity.ok(
                    Map.of("success", true, "data", response)
            );
        } catch (Exception e) {
            log.error("Error fetching exam detail {}: {}", examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // CHECK CAN ATTEMPT
    // GET /api/v1/exams/{examId}/can-attempt
    // ============================================
    @GetMapping("/{examId}/can-attempt")
    public ResponseEntity<?> canAttempt(
            @PathVariable String examId,
            @CurrentUserId String userId) {
        try {
            boolean canAttempt =
                    examStudentService.canUserAttempt(examId, userId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "canAttempt", canAttempt
                    )
            );
        } catch (Exception e) {
            log.error("Error checking attempt for exam {}: {}",
                    examId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET USER ATTEMPT HISTORY
    // GET /api/v1/exams/history
    // ============================================
    @GetMapping("/history")
    public ResponseEntity<?> getAttemptHistory(
            @CurrentUserId String userId) {
        try {
            List<ExamAttemptHistoryResponse> history =
                    examStudentService.getUserAttemptHistory(userId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", history,
                            "total", history.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching history for user {}: {}",
                    userId, e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }

    // ============================================
    // GET ATTEMPT HISTORY FOR SPECIFIC EXAM
    // GET /api/v1/exams/{examId}/history
    // ============================================
    @GetMapping("/{examId}/history")
    public ResponseEntity<?> getExamAttemptHistory(
            @PathVariable String examId,
            @CurrentUserId String userId) {
        try {
            List<ExamAttemptHistoryResponse> history =
                    examStudentService.getUserExamAttemptHistory(userId, examId);
            return ResponseEntity.ok(
                    Map.of(
                            "success", true,
                            "data", history,
                            "total", history.size()
                    )
            );
        } catch (Exception e) {
            log.error("Error fetching exam history: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    Map.of("success", false, "message", e.getMessage())
            );
        }
    }
}