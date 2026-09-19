package com.examplatform.modules.guide.mapper;

import com.examplatform.modules.guide.dto.GuidePracticeCqAdminRequest;
import com.examplatform.modules.guide.dto.GuidePracticeCqPartItem;
import com.examplatform.modules.guide.dto.GuidePracticeCqPartResponse;
import com.examplatform.modules.guide.dto.GuidePracticeCqResponse;
import com.examplatform.modules.guide.entity.GuidePracticeCq;
import com.examplatform.modules.guide.entity.GuidePracticeCqPart;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GuidePracticeCqMapper {

    public GuidePracticeCq applyRequest(GuidePracticeCq cq, GuidePracticeCqAdminRequest req) {
        cq.setStimulus(req.getStimulus());
        cq.setStimulusBn(req.getStimulusBn());
        cq.setBoardQuestion(req.isBoardQuestion());
        cq.setBoard(req.getBoard());
        cq.setExamYear(req.getExamYear());

        List<GuidePracticeCqPart> newParts = buildParts(cq, req.getParts());
        cq.getParts().clear();
        cq.getParts().addAll(newParts);

        Integer total = req.getTotalMaxMark();
        if (total == null) {
            int sum = 0;
            for (GuidePracticeCqPart p : newParts) {
                if (p.getMaxMark() != null) sum += p.getMaxMark();
            }
            total = sum > 0 ? sum : null;
        }
        cq.setTotalMaxMark(total);
        cq.setSortOrder(req.getSortOrder());
        return cq;
    }

    private List<GuidePracticeCqPart> buildParts(GuidePracticeCq cq, List<GuidePracticeCqPartItem> items) {
        List<GuidePracticeCqPart> parts = new ArrayList<>();
        if (items == null) return parts;

        int order = 1;
        for (GuidePracticeCqPartItem item : items) {
            parts.add(GuidePracticeCqPart.builder()
                    .cq(cq)
                    .partOrder(order++)
                    .questionText(item.getQuestionText())
                    .modelAnswer(item.getModelAnswer())
                    .markingScheme(item.getMarkingScheme())
                    .maxMark(item.getMaxMark())
                    .build());
        }
        return parts;
    }

    public GuidePracticeCqResponse toResponse(GuidePracticeCq cq) {
        return GuidePracticeCqResponse.builder()
                .id(cq.getId())
                .topicId(cq.getTopic().getId())
                .stimulus(cq.getStimulus())
                .stimulusBn(cq.getStimulusBn())
                .isBoardQuestion(cq.isBoardQuestion())
                .board(cq.getBoard())
                .examYear(cq.getExamYear())
                .parts(cq.getParts().stream()
                        .map(p -> GuidePracticeCqPartResponse.builder()
                                .id(p.getId())
                                .partOrder(p.getPartOrder())
                                .questionText(p.getQuestionText())
                                .modelAnswer(p.getModelAnswer())
                                .markingScheme(p.getMarkingScheme())
                                .maxMark(p.getMaxMark())
                                .build())
                        .toList())
                .totalMaxMark(cq.getTotalMaxMark())
                .sortOrder(cq.getSortOrder())
                .build();
    }
}
