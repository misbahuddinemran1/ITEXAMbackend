package com.examplatform.modules.written.question.service;

import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.ReorderQuestionsRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.response.QuestionWithAnswerResponse;

import java.util.List;

public interface WrittenQuestionService {

    QuestionAdminResponse createQuestion(CreateQuestionRequest request);

    QuestionAdminResponse updateQuestion(String questionId, UpdateQuestionRequest request);

    void reorderQuestions(String examId, ReorderQuestionsRequest request);

    void deleteQuestion(String questionId);

    List<QuestionAdminResponse> getQuestionsForAdmin(String examId);

    List<QuestionStudentResponse> getQuestionsForStudent(String examId);

    QuestionAdminResponse getQuestionByIdForAdmin(String questionId);

    /**
     * Returns questions with model/AI answers for a FINISHED exam only.
     * Server-side verifies the exam has actually ended before exposing any answer.
     */
    List<QuestionWithAnswerResponse> getQuestionsWithAnswers(String examId);
}
