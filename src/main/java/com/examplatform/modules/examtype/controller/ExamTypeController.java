package com.examplatform.modules.examtype.controller;

import com.examplatform.common.dto.ApiResponse;
import com.examplatform.modules.examtype.dto.request.ExamTypeRequest;
import com.examplatform.modules.examtype.dto.response.ExamTypeResponse;
import com.examplatform.modules.examtype.service.ExamTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ExamTypeController {

    private final ExamTypeService examTypeService;

    @GetMapping("/pub/exam-types")
    public ResponseEntity<ApiResponse<List<ExamTypeResponse>>>
            getAllExamTypes() {
        return ResponseEntity.ok(
            ApiResponse.success(examTypeService.getAllExamTypes())
        );
    }

    @GetMapping("/pub/exam-types/{id}")
    public ResponseEntity<ApiResponse<ExamTypeResponse>>
            getExamType(@PathVariable String id) {
        return ResponseEntity.ok(
            ApiResponse.success(examTypeService.getExamType(id))
        );
    }

    @GetMapping("/admin/exam-types")
    public ResponseEntity<ApiResponse<List<ExamTypeResponse>>>
            getAllExamTypesAdmin() {
        return ResponseEntity.ok(
            ApiResponse.success(examTypeService.getAllExamTypesAdmin())
        );
    }

    @PostMapping("/admin/exam-types")
    public ResponseEntity<ApiResponse<ExamTypeResponse>>
            createExamType(
                @Valid @RequestBody ExamTypeRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                    "ExamType created",
                    examTypeService.createExamType(request)
                ));
    }

    @PutMapping("/admin/exam-types/{id}")
    public ResponseEntity<ApiResponse<ExamTypeResponse>>
            updateExamType(
                @PathVariable String id,
                @Valid @RequestBody ExamTypeRequest request) {
        return ResponseEntity.ok(
            ApiResponse.success(
                "ExamType updated",
                examTypeService.updateExamType(id, request)
            )
        );
    }

    @DeleteMapping("/admin/exam-types/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deleteExamType(@PathVariable String id) {
        examTypeService.deleteExamType(id);
        return ResponseEntity.ok(
            ApiResponse.success("ExamType deleted", null)
        );
    }
}