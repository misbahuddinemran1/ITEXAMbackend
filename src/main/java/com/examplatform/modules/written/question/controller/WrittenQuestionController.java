package com.examplatform.modules.written.question.controller;

import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.response.QuestionWithAnswerResponse;
import com.examplatform.modules.written.question.service.WrittenQuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/written/questions")
@RequiredArgsConstructor
public class WrittenQuestionController {

    private final WrittenQuestionService questionService;

    @GetMapping("/exam/{examId}")
    public List<QuestionStudentResponse> getQuestionsForExam(@PathVariable String examId) {
        return questionService.getQuestionsForStudent(examId);
    }

    /**
     * Questions + model/AI answers for a FINISHED exam only.
     * Service throws IllegalStateException if the exam hasn't ended yet.
     */
    @GetMapping("/exam/{examId}/answers")
    public List<QuestionWithAnswerResponse> getQuestionsWithAnswers(@PathVariable String examId) {
        try {
            return questionService.getQuestionsWithAnswers(examId);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
