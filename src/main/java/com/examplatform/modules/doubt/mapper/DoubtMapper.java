package com.examplatform.modules.doubt.mapper;

import com.examplatform.modules.doubt.dto.DoubtResponse;
import com.examplatform.modules.doubt.dto.DoubtSummaryResponse;
import com.examplatform.modules.doubt.entity.DoubtAnswer;
import com.examplatform.modules.doubt.entity.DoubtQuestion;
import org.springframework.stereotype.Component;

@Component
public class DoubtMapper {

    public DoubtResponse toResponse(DoubtQuestion q, DoubtAnswer a) {
        DoubtResponse.DoubtResponseBuilder b = DoubtResponse.builder()
                .id(q.getId())
                .studentUserId(q.getStudentUserId())
                .subjectId(q.getSubjectId())
                .chapterId(q.getChapterId())
                .topicId(q.getTopicId())
                .questionText(q.getQuestionText())
                .questionImageUrl(q.getQuestionImageUrl())
                .questionPdfUrl(q.getQuestionPdfUrl())
                .status(q.getStatus())
                .createdAt(q.getCreatedAt())
                .updatedAt(q.getUpdatedAt());

        if (a != null) {
            b.answerText(a.getAnswerText())
             .answerPdfUrl(a.getAnswerPdfUrl())
             .answeredViaAi(a.getAnsweredViaAi());
        }
        return b.build();
    }

    public DoubtSummaryResponse toSummary(DoubtQuestion q) {
        String preview = q.getQuestionText();
        if (preview != null && preview.length() > 120) {
            preview = preview.substring(0, 120) + "...";
        }
        return DoubtSummaryResponse.builder()
                .id(q.getId())
                .chapterId(q.getChapterId())
                .questionText(preview)
                .hasImage(q.getQuestionImageUrl() != null)
                .hasPdf(q.getQuestionPdfUrl() != null)
                .status(q.getStatus())
                .createdAt(q.getCreatedAt())
                .build();
    }
}
