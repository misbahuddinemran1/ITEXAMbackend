package com.examplatform.modules.doubt.service;

import com.examplatform.modules.doubt.dto.*;

import java.util.List;

public interface DoubtService {
    DoubtResponse createDoubt(String studentUserId, CreateDoubtRequest request);
    String uploadQuestionFile(String doubtId, String studentUserId, org.springframework.web.multipart.MultipartFile file, boolean isPdf);
    DoubtResponse updateDoubt(String doubtId, String studentUserId, UpdateDoubtRequest request);
    List<DoubtSummaryResponse> getMyDoubts(String studentUserId);
    List<DoubtSummaryResponse> getAnsweredDoubts(String chapterId, String subjectId);
    DoubtResponse getDoubtDetail(String doubtId, String requesterUserId);
    DoubtResponse getDoubtDetailForAdmin(String doubtId);
}
