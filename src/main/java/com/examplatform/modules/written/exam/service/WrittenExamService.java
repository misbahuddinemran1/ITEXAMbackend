package com.examplatform.modules.written.exam.service;

import com.examplatform.modules.written.exam.request.CreateExamRequest;
import com.examplatform.modules.written.exam.request.ReopenExamRequest;
import com.examplatform.modules.written.exam.request.UpdateExamRequest;
import com.examplatform.modules.written.exam.response.ExamResponse;
import com.examplatform.modules.written.exam.response.ExamSummaryResponse;

import java.util.List;

public interface WrittenExamService {

    ExamResponse createExam(CreateExamRequest request, String adminId);

    ExamResponse updateExam(String examId, UpdateExamRequest request);

    ExamResponse publishExam(String examId);

    ExamResponse goLive(String examId);

    ExamResponse endExam(String examId);

    ExamResponse archiveExam(String examId);

    ExamResponse reopenExam(String examId, ReopenExamRequest request);

    ExamResponse getExamById(String examId);

    List<ExamSummaryResponse> getLiveExamsForStudent(String userId, String educationLevel);

    // নতুন: সময় শেষ হয়ে যাওয়া (LIVE status কিন্তু endTime পার হয়ে গেছে) exam গুলো — practice এর জন্য
    List<ExamSummaryResponse> getFinishedExamsForStudent(String userId, String educationLevel);

    List<ExamResponse> getAllExamsForAdmin();

    void deleteExam(String examId);
}
