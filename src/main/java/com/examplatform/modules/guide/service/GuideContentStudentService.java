package com.examplatform.modules.guide.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.guide.dto.GuidePracticeCqResponse;
import com.examplatform.modules.guide.dto.GuidePracticeMcqOptionResponse;
import com.examplatform.modules.guide.dto.GuidePracticeMcqResponse;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.entity.GuidePracticeCq;
import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import com.examplatform.modules.guide.entity.GuidePracticeMcqOption;
import com.examplatform.modules.guide.repository.GuideContentRepository;
import com.examplatform.modules.guide.repository.GuidePracticeCqRepository;
import com.examplatform.modules.guide.repository.GuidePracticeMcqOptionRepository;
import com.examplatform.modules.guide.repository.GuidePracticeMcqRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideContentStudentService {

    private final GuideContentRepository guideContentRepository;
    private final GuidePracticeMcqRepository guidePracticeMcqRepository;
    private final GuidePracticeMcqOptionRepository guidePracticeMcqOptionRepository;
    private final GuidePracticeCqRepository guidePracticeCqRepository;

    /**
     * Fetch published guide content for a topic. Read screen entry point.
     */
    @Transactional
    public GuideContent getPublishedContent(String topicId) {
        return guideContentRepository
                .findByTopicIdAndStatus(topicId, GuideContent.GuideStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No published guide content for topic: " + topicId));
    }

    /**
     * Practice Options screen — MCQ list for the topic.
     * Sourced entirely from guide_practice_mcq — fully isolated from the
     * Exam module's question bank so guide practice never leaks
     * upcoming/unreleased exam questions.
     */
    @Transactional
    public List<GuidePracticeMcqResponse> getMcqForTopic(String topicId) {
        List<GuidePracticeMcq> list = guidePracticeMcqRepository.findByTopicIdOrderBySortOrderAsc(topicId);
        return list.stream().map(this::toMcqResponse).toList();
    }

    /**
     * Practice Options screen — Previous Year MCQ (board only) for the topic.
     */
    @Transactional
    public List<GuidePracticeMcqResponse> getBoardMcqForTopic(String topicId) {
        List<GuidePracticeMcq> list = guidePracticeMcqRepository
                .findByTopicIdAndIsBoardQuestionTrueOrderBySortOrderAsc(topicId);
        return list.stream().map(this::toMcqResponse).toList();
    }

    /**
     * Practice Options screen — all CQ (board + non-board) for the topic.
     * Sourced entirely from guide_practice_cq — fully isolated from the
     * Written Exam module's question bank.
     */
    @Transactional
    public List<GuidePracticeCqResponse> getCqForTopic(String topicId) {
        List<GuidePracticeCq> list = guidePracticeCqRepository.findByTopicIdOrderBySortOrderAsc(topicId);
        return list.stream().map(this::toCqResponse).toList();
    }

    /**
     * Practice Options screen — Previous Year CQ (board only) for the topic.
     */
    @Transactional
    public List<GuidePracticeCqResponse> getBoardQuestionsForTopic(String topicId) {
        List<GuidePracticeCq> list = guidePracticeCqRepository
                .findByTopicIdAndIsBoardQuestionTrueOrderBySortOrderAsc(topicId);
        return list.stream().map(this::toCqResponse).toList();
    }

    // ---- mappers: entity -> DTO (never expose entities/Topic/Chapter/Subject directly) ----

    private GuidePracticeMcqResponse toMcqResponse(GuidePracticeMcq mcq) {
        List<GuidePracticeMcqOption> options =
                guidePracticeMcqOptionRepository.findAllByMcqIdOrderByOrderIndexAsc(mcq.getId());
        return GuidePracticeMcqResponse.builder()
                .id(mcq.getId())
                .topicId(mcq.getTopic().getId())
                .questionText(mcq.getQuestionText())
                .questionTextBn(mcq.getQuestionTextBn())
                .isBoardQuestion(mcq.isBoardQuestion())
                .board(mcq.getBoard())
                .yearAppeared(mcq.getYearAppeared())
                .sortOrder(mcq.getSortOrder())
                .options(options.stream().map(o -> GuidePracticeMcqOptionResponse.builder()
                        .id(o.getId())
                        .optionKey(o.getOptionKey())
                        .optionText(o.getOptionText())
                        .optionTextBn(o.getOptionTextBn())
                        .isCorrect(o.isCorrect())
                        .explanation(o.getExplanation())
                        .orderIndex(o.getOrderIndex())
                        .build()).toList())
                .build();
    }

    private GuidePracticeCqResponse toCqResponse(GuidePracticeCq cq) {
        return GuidePracticeCqResponse.builder()
                .id(cq.getId())
                .topicId(cq.getTopic().getId())
                .stimulus(cq.getStimulus())
                .stimulusBn(cq.getStimulusBn())
                .isBoardQuestion(cq.isBoardQuestion())
                .board(cq.getBoard())
                .examYear(cq.getExamYear())
                .partAQuestion(cq.getPartAQuestion())
                .partAModelAnswer(cq.getPartAModelAnswer())
                .partAMarkingScheme(cq.getPartAMarkingScheme())
                .partAMaxMark(cq.getPartAMaxMark())
                .partBQuestion(cq.getPartBQuestion())
                .partBModelAnswer(cq.getPartBModelAnswer())
                .partBMarkingScheme(cq.getPartBMarkingScheme())
                .partBMaxMark(cq.getPartBMaxMark())
                .partCQuestion(cq.getPartCQuestion())
                .partCModelAnswer(cq.getPartCModelAnswer())
                .partCMarkingScheme(cq.getPartCMarkingScheme())
                .partCMaxMark(cq.getPartCMaxMark())
                .partDQuestion(cq.getPartDQuestion())
                .partDModelAnswer(cq.getPartDModelAnswer())
                .partDMarkingScheme(cq.getPartDMarkingScheme())
                .partDMaxMark(cq.getPartDMaxMark())
                .totalMaxMark(cq.getTotalMaxMark())
                .sortOrder(cq.getSortOrder())
                .build();
    }
}
