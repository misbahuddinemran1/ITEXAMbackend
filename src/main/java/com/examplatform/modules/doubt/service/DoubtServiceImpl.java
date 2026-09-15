package com.examplatform.modules.doubt.service;

import com.examplatform.modules.doubt.dto.*;
import com.examplatform.modules.doubt.entity.DoubtAnswer;
import com.examplatform.modules.doubt.entity.DoubtQuestion;
import com.examplatform.modules.doubt.enums.DoubtStatus;
import com.examplatform.modules.doubt.mapper.DoubtMapper;
import com.examplatform.modules.doubt.repository.DoubtAnswerRepository;
import com.examplatform.modules.doubt.repository.DoubtQuestionRepository;
import com.examplatform.modules.written.submission.service.ImageKitUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoubtServiceImpl implements DoubtService {

    private final DoubtQuestionRepository doubtQuestionRepository;
    private final DoubtAnswerRepository doubtAnswerRepository;
    private final ImageKitUploadService imageKitUploadService;
    private final DoubtMapper doubtMapper;

    @Override
    @Transactional
    public DoubtResponse createDoubt(String studentUserId, CreateDoubtRequest request) {
        if (request.getChapterId() == null || request.getChapterId().isBlank()) {
            throw new IllegalArgumentException("chapterId is required");
        }

        DoubtQuestion doubt = DoubtQuestion.builder()
                .studentUserId(studentUserId)
                .subjectId(request.getSubjectId())
                .chapterId(request.getChapterId())
                .topicId(request.getTopicId())
                .questionText(request.getQuestionText())
                .status(DoubtStatus.PENDING)
                .build();

        doubtQuestionRepository.save(doubt);
        return doubtMapper.toResponse(doubt, null);
    }

    @Override
    @Transactional
    public String uploadQuestionFile(String doubtId, String studentUserId, MultipartFile file, boolean isPdf) {
        DoubtQuestion doubt = getOwnedDoubtOrThrow(doubtId, studentUserId);
        assertEditable(doubt);

        String fileUrl = imageKitUploadService.uploadFile(file, "doubts/" + doubtId);

        if (isPdf) {
            doubt.setQuestionPdfUrl(fileUrl);
        } else {
            doubt.setQuestionImageUrl(fileUrl);
        }
        doubtQuestionRepository.save(doubt);
        return fileUrl;
    }

    @Override
    @Transactional
    public DoubtResponse updateDoubt(String doubtId, String studentUserId, UpdateDoubtRequest request) {
        DoubtQuestion doubt = getOwnedDoubtOrThrow(doubtId, studentUserId);
        assertEditable(doubt);

        if (request.getSubjectId() != null) doubt.setSubjectId(request.getSubjectId());
        if (request.getChapterId() != null) doubt.setChapterId(request.getChapterId());
        if (request.getTopicId() != null) doubt.setTopicId(request.getTopicId());
        if (request.getQuestionText() != null) doubt.setQuestionText(request.getQuestionText());

        doubtQuestionRepository.save(doubt);
        return doubtMapper.toResponse(doubt, null);
    }

    @Override
    public List<DoubtSummaryResponse> getMyDoubts(String studentUserId) {
        return doubtQuestionRepository.findByStudentUserIdOrderByCreatedAtDesc(studentUserId)
                .stream()
                .map(doubtMapper::toSummary)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoubtSummaryResponse> getAnsweredDoubts(String chapterId, String subjectId) {
        List<DoubtQuestion> list;
        if (chapterId != null && !chapterId.isBlank()) {
            list = doubtQuestionRepository.findByStatusAndChapterIdOrderByCreatedAtDesc(DoubtStatus.ANSWERED, chapterId);
        } else {
            list = doubtQuestionRepository.findByStatusOrderByCreatedAtDesc(DoubtStatus.ANSWERED);
        }
        return list.stream().map(doubtMapper::toSummary).collect(Collectors.toList());
    }

    @Override
    public DoubtResponse getDoubtDetail(String doubtId, String requesterUserId) {
        DoubtQuestion doubt = doubtQuestionRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found: " + doubtId));

        // নিজের doubt না হলে, শুধু ANSWERED হলেই দেখতে পারবে (public knowledge base)
        if (!doubt.getStudentUserId().equals(requesterUserId) && doubt.getStatus() != DoubtStatus.ANSWERED) {
            throw new IllegalStateException("Not authorized to view this doubt");
        }

        DoubtAnswer answer = doubtAnswerRepository.findByDoubtQuestionId(doubtId).orElse(null);
        return doubtMapper.toResponse(doubt, answer);
    }
    
@Override
public DoubtResponse getDoubtDetailForAdmin(String doubtId) {
    DoubtQuestion doubt = doubtQuestionRepository.findById(doubtId)
            .orElseThrow(() -> new IllegalArgumentException("Doubt not found: " + doubtId));

    DoubtAnswer answer = doubtAnswerRepository.findByDoubtQuestionId(doubtId).orElse(null);
    return doubtMapper.toResponse(doubt, answer);
}
    private DoubtQuestion getOwnedDoubtOrThrow(String doubtId, String studentUserId) {
        DoubtQuestion doubt = doubtQuestionRepository.findById(doubtId)
                .orElseThrow(() -> new IllegalArgumentException("Doubt not found: " + doubtId));
        if (!doubt.getStudentUserId().equals(studentUserId)) {
            throw new IllegalStateException("Not authorized to modify this doubt");
        }
        return doubt;
    }

    private void assertEditable(DoubtQuestion doubt) {
        if (doubt.getStatus() == DoubtStatus.ANSWERED) {
            throw new IllegalStateException("Answered doubt can no longer be edited");
        }
    }
}
