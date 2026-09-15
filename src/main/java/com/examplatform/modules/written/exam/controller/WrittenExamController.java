package com.examplatform.modules.written.exam.controller;

import com.examplatform.modules.written.exam.response.ExamSummaryResponse;
import com.examplatform.modules.written.exam.service.WrittenExamService;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/written/exams")
@RequiredArgsConstructor
public class WrittenExamController {

    private final WrittenExamService examService;

    @GetMapping("/live")
    public List<ExamSummaryResponse> getLiveExams(
            @RequestParam String educationLevel,
            Authentication auth) {
        String userId = auth.getName();
        return examService.getLiveExamsForStudent(userId, educationLevel);
    }

    @GetMapping("/finished")
    public List<ExamSummaryResponse> getFinishedExams(
            @RequestParam String educationLevel,
            Authentication auth) {
        String userId = auth.getName();
        return examService.getFinishedExamsForStudent(userId, educationLevel);
    }
}
