package com.examplatform.modules.question.controller.admin;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.common.dto.PageResponse;
import com.examplatform.modules.question.dto.request.QuestionCreateRequest;
import com.examplatform.modules.question.dto.response.QuestionResponse;
import com.examplatform.modules.question.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class QuestionAdminController {

    private final QuestionService questionService;

    @PostMapping("/admin/questions")
    public ResponseEntity<ApiResponse<QuestionResponse>>
            createQuestion(
                @Valid @RequestBody QuestionCreateRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "Question created",
                    questionService.createQuestion(request)
                ));
    }

    @GetMapping("/admin/questions")
    public ResponseEntity<ApiResponse<PageResponse<QuestionResponse>>>
    getQuestions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subjectId,
            @RequestParam(required = false) String chapterId,
            @RequestParam(required = false) String topicId,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(
            ApiResponse.success(
                    questionService.getQuestions(
                            status,
                            subjectId,
                            chapterId,
                            topicId,
                            difficulty,
                            year,
                            source,
                            keyword,
                            page,
                            size
                    )
            )
        );
    }

    @GetMapping("/admin/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>>
            getQuestion(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success(
                questionService.getQuestion(id)
            )
        );
    }

    @PatchMapping("/admin/questions/{id}/submit-review")
    public ResponseEntity<ApiResponse<QuestionResponse>>
            submitReview(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Submitted for review",
                questionService.updateStatus(
                    id, "UNDER_REVIEW", null)
            )
        );
    }

    @PatchMapping("/admin/questions/{id}/approve")
    public ResponseEntity<ApiResponse<QuestionResponse>>
            approve(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Question approved",
                questionService.updateStatus(
                    id, "APPROVED", null)
            )
        );
    }

    @PatchMapping("/admin/questions/{id}/reject")
    public ResponseEntity<ApiResponse<QuestionResponse>>
            reject(@PathVariable String id,
                   @RequestParam String notes) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "Question rejected",
                questionService.updateStatus(
                    id, "REJECTED", notes)
            )
        );
    }

    @PutMapping("/admin/questions/{id}")
    public ResponseEntity<ApiResponse<QuestionResponse>>
    updateQuestion(
            @PathVariable String id,
            @Valid @RequestBody QuestionCreateRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Question updated",
                        questionService.updateQuestion(
                                id,
                                request
                        )
                )
        );
    }

    @DeleteMapping("/admin/questions/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(
            ApiResponse.success("Question archived", null)
        );
    }
}