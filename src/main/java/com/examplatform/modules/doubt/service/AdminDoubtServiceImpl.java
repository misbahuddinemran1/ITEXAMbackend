package com.examplatform.modules.doubt.service;

import com.examplatform.modules.auth.entity.AdminUser;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.doubt.dto.*;
import com.examplatform.modules.doubt.entity.DoubtAnswer;
import com.examplatform.modules.doubt.entity.DoubtQuestion;
import com.examplatform.modules.doubt.enums.DoubtStatus;
import com.examplatform.modules.doubt.mapper.DoubtMapper;
import com.examplatform.modules.doubt.repository.DoubtAnswerRepository;
import com.examplatform.modules.doubt.repository.DoubtQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDoubtServiceImpl implements AdminDoubtService {

    private final DoubtQuestionRepository doubtQuestionRepository;
    private final DoubtAnswerRepository doubtAnswerRepository;
    private final DoubtMapper doubtMapper;
    private final DoubtAiAnswerService doubtAiAnswerService;
    private final AdminUserRepository adminUserRepository;

    @Override
    public List<DoubtSummaryResponse> getByStatus(String status) {
        DoubtStatus statusEnum = DoubtStatus.valueOf(status.toUpperCase());
        return doubtQuestionRepository.findByStatusOrderByCreatedAtAsc(statusEnum)
                .stream()
                .map(doubtMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoubtResponse acceptDoubt(String doubtId, String adminUsername) {
        DoubtQuestion doubt = getDoubtOrThrow(doubtId);
        if (doubt.getStatus() != DoubtStatus.PENDING) {
            throw new IllegalStateException("Only PENDING doubts can be accepted");
        }
        String adminId = resolveAdminId(adminUsername);
        doubt.setStatus(DoubtStatus.REVIEWED);
        doubt.setReviewedAt(LocalDateTime.now());
        doubt.setReviewedByAdminId(adminId);
        doubtQuestionRepository.save(doubt);
        return doubtMapper.toResponse(doubt, null);
    }

    @Override
    public AiGenerateResponse generateAiPreview(String doubtId) {
        DoubtQuestion doubt = getDoubtOrThrow(doubtId);
        String generatedText = doubtAiAnswerService.generateAnswerText(doubt);

        return AiGenerateResponse.builder()
                .generatedText(generatedText)
                .build();
    }

    @Override
    @Transactional
    public DoubtResponse saveAnswer(String doubtId, String adminUsername, AdminAnswerRequest request) {
        DoubtQuestion doubt = getDoubtOrThrow(doubtId);
        if (doubt.getStatus() == DoubtStatus.PENDING) {
            throw new IllegalStateException("Accept the doubt before answering");
        }
        if ((request.getAnswerText() == null || request.getAnswerText().isBlank())
                && (request.getAnswerPdfUrl() == null || request.getAnswerPdfUrl().isBlank())) {
            throw new IllegalArgumentException("Either answerText or answerPdfUrl must be provided");
        }

        String adminId = resolveAdminId(adminUsername);

        DoubtAnswer answer = doubtAnswerRepository.findByDoubtQuestionId(doubtId)
                .orElse(DoubtAnswer.builder().doubtQuestionId(doubtId).build());

        answer.setAdminId(adminId);
        answer.setAnswerText(request.getAnswerText());
        answer.setAnswerPdfUrl(request.getAnswerPdfUrl());
        answer.setAnsweredViaAi(request.isUseAiText());

        doubtAnswerRepository.save(answer);

        doubt.setStatus(DoubtStatus.ANSWERED);
        doubtQuestionRepository.save(doubt);

        return doubtMapper.toResponse(doubt, answer);
    }

    private String resolveAdminId(String adminUsername) {
        AdminUser adminUser = adminUserRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalStateException("Admin user not found: " + adminUsername));
        return adminUser.getId();
    }

    private DoubtQuestion getDoubtOrThrow(String doubtId) {
        return doubtQuestionRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found: " + doubtId));
    }
}
