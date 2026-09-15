package com.examplatform.modules.written.question.controller;

import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.ReorderQuestionsRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.service.WrittenQuestionService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/written/questions")
@RequiredArgsConstructor
public class AdminWrittenQuestionController {

    private final WrittenQuestionService questionService;

    @PostMapping
    public QuestionAdminResponse createQuestion(@RequestBody CreateQuestionRequest request) {
        return questionService.createQuestion(request);
    }

    @PutMapping("/{questionId}")
    public QuestionAdminResponse updateQuestion(
            @PathVariable String questionId,
            @RequestBody UpdateQuestionRequest request) {
        return questionService.updateQuestion(questionId, request);
    }

    @PatchMapping("/exam/{examId}/reorder")
    public void reorderQuestions(@PathVariable String examId, @RequestBody ReorderQuestionsRequest request) {
        questionService.reorderQuestions(examId, request);
    }

    @DeleteMapping("/{questionId}")
    public void deleteQuestion(@PathVariable String questionId) {
        questionService.deleteQuestion(questionId);
    }

    @GetMapping("/exam/{examId}")
    public List<QuestionAdminResponse> getQuestionsForExam(@PathVariable String examId) {
        return questionService.getQuestionsForAdmin(examId);
    }

    @GetMapping("/{questionId}")
    public QuestionAdminResponse getQuestionById(@PathVariable String questionId) {
        return questionService.getQuestionByIdForAdmin(questionId);
    }
}