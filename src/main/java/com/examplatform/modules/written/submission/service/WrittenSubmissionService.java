package com.examplatform.modules.written.submission.service;

import com.examplatform.modules.written.submission.request.StartExamRequest;
import com.examplatform.modules.written.submission.request.SubmitExamRequest;
import com.examplatform.modules.written.submission.request.SubmitTextAnswersRequest;
import com.examplatform.modules.written.submission.request.UploadSubmissionFileRequest;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;

import java.util.List;

public interface WrittenSubmissionService {

    SubmissionResponse startExam(String userId, StartExamRequest request);

    SubmissionFileResponse uploadFile(String submissionId, String userId, UploadSubmissionFileRequest request);

    void submitTextAnswers(String submissionId, String userId, SubmitTextAnswersRequest request);

    SubmissionResponse submitExam(String submissionId, String userId, SubmitExamRequest request);

    SubmissionResponse getSubmissionById(String submissionId, String userId);

    List<SubmissionFileResponse> getSubmissionFiles(String submissionId, String userId);

    List<SubmissionResponse> getMySubmissions(String userId);

    List<SubmissionResponse> getSubmissionsForExam(String examId); // admin
}
