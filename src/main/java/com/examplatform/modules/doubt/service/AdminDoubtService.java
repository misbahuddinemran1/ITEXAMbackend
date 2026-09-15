package com.examplatform.modules.doubt.service;

import com.examplatform.modules.doubt.dto.AdminAnswerRequest;
import com.examplatform.modules.doubt.dto.AiGenerateResponse;
import com.examplatform.modules.doubt.dto.DoubtResponse;
import com.examplatform.modules.doubt.dto.DoubtSummaryResponse;

import java.util.List;

public interface AdminDoubtService {
    List<DoubtSummaryResponse> getByStatus(String status);
    DoubtResponse acceptDoubt(String doubtId, String adminId);
    AiGenerateResponse generateAiPreview(String doubtId);
    DoubtResponse saveAnswer(String doubtId, String adminId, AdminAnswerRequest request);
}
