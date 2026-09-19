package com.examplatform.modules.guide.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.guide.dto.*;
import com.examplatform.modules.guide.entity.GuidePracticeCq;
import com.examplatform.modules.guide.mapper.GuidePracticeCqMapper;
import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import com.examplatform.modules.guide.entity.GuidePracticeMcqOption;
import com.examplatform.modules.guide.repository.GuidePracticeCqRepository;
import com.examplatform.modules.guide.repository.GuidePracticeMcqOptionRepository;
import com.examplatform.modules.guide.repository.GuidePracticeMcqRepository;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuidePracticeAdminService {

    private final GuidePracticeMcqRepository mcqRepository;
    private final GuidePracticeMcqOptionRepository mcqOptionRepository;
    private final GuidePracticeCqRepository cqRepository;
    private final TopicRepository topicRepository;
    private final GuidePracticeCqMapper cqMapper;

    // ================= MCQ =================

    @Transactional
    public GuidePracticeMcqResponse createMcq(GuidePracticeMcqAdminRequest req) {
        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + req.getTopicId()));

        GuidePracticeMcq mcq = GuidePracticeMcq.builder()
                .topic(topic)
                .questionText(req.getQuestionText())
                .questionTextBn(req.getQuestionTextBn())
                .isBoardQuestion(req.isBoardQuestion())
                .board(req.getBoard())
                .yearAppeared(req.getYearAppeared())
                .sortOrder(req.getSortOrder())
                .build();
        mcq = mcqRepository.save(mcq);

        attachOptions(mcq, req.getOptions());
        return toMcqResponse(mcq);
    }

    @Transactional
    public GuidePracticeMcqResponse updateMcq(String id, GuidePracticeMcqAdminRequest req) {
        GuidePracticeMcq mcq = mcqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MCQ not found: " + id));

        if (req.getTopicId() != null && !req.getTopicId().equals(mcq.getTopic().getId())) {
            Topic topic = topicRepository.findById(req.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + req.getTopicId()));
            mcq.setTopic(topic);
        }
        mcq.setQuestionText(req.getQuestionText());
        mcq.setQuestionTextBn(req.getQuestionTextBn());
        mcq.setBoardQuestion(req.isBoardQuestion());
        mcq.setBoard(req.getBoard());
        mcq.setYearAppeared(req.getYearAppeared());
        mcq.setSortOrder(req.getSortOrder());
        mcq = mcqRepository.save(mcq);

        mcqOptionRepository.deleteAllByMcqId(mcq.getId());
        attachOptions(mcq, req.getOptions());

        return toMcqResponse(mcq);
    }

    @Transactional
    public void deleteMcq(String id) {
        if (!mcqRepository.existsById(id)) {
            throw new ResourceNotFoundException("MCQ not found: " + id);
        }
        mcqRepository.deleteById(id);
    }

    @Transactional
    public List<GuidePracticeMcqResponse> listMcq(String topicId) {
        List<GuidePracticeMcq> list = (topicId != null && !topicId.isBlank())
                ? mcqRepository.findByTopicIdOrderBySortOrderAsc(topicId)
                : mcqRepository.findAllByOrderBySortOrderAsc();
        return list.stream().map(this::toMcqResponse).toList();
    }

    private void attachOptions(GuidePracticeMcq mcq, List<GuidePracticeMcqAdminRequest.OptionItem> items) {
        if (items == null) return;
        List<GuidePracticeMcqOption> options = new ArrayList<>();
        for (GuidePracticeMcqAdminRequest.OptionItem item : items) {
            options.add(GuidePracticeMcqOption.builder()
                    .mcq(mcq)
                    .optionKey(item.getOptionKey())
                    .optionText(item.getOptionText())
                    .optionTextBn(item.getOptionTextBn())
                    .isCorrect(item.isCorrect())
                    .explanation(item.getExplanation())
                    .orderIndex(item.getOrderIndex())
                    .build());
        }
        mcqOptionRepository.saveAll(options);
    }

    private GuidePracticeMcqResponse toMcqResponse(GuidePracticeMcq mcq) {
        List<GuidePracticeMcqOption> options = mcqOptionRepository.findAllByMcqIdOrderByOrderIndexAsc(mcq.getId());
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

    // ================= CQ =================

    @Transactional
    public GuidePracticeCqResponse createCq(GuidePracticeCqAdminRequest req) {
        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + req.getTopicId()));

        GuidePracticeCq cq = cqMapper.applyRequest(new GuidePracticeCq(), req);
        cq.setTopic(topic);
        cq = cqRepository.save(cq);
        return cqMapper.toResponse(cq);
    }

    @Transactional
    public GuidePracticeCqResponse updateCq(String id, GuidePracticeCqAdminRequest req) {
        GuidePracticeCq cq = cqRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CQ not found: " + id));

        if (req.getTopicId() != null && !req.getTopicId().equals(cq.getTopic().getId())) {
            Topic topic = topicRepository.findById(req.getTopicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + req.getTopicId()));
            cq.setTopic(topic);
        }
        cq = cqMapper.applyRequest(cq, req);
        cq = cqRepository.save(cq);
        return cqMapper.toResponse(cq);
    }

    @Transactional
    public void deleteCq(String id) {
        if (!cqRepository.existsById(id)) {
            throw new ResourceNotFoundException("CQ not found: " + id);
        }
        cqRepository.deleteById(id);
    }

    @Transactional
    public List<GuidePracticeCqResponse> listCq(String topicId) {
        List<GuidePracticeCq> list = (topicId != null && !topicId.isBlank())
                ? cqRepository.findByTopicIdOrderBySortOrderAsc(topicId)
                : cqRepository.findAllByOrderBySortOrderAsc();
        return list.stream().map(cqMapper::toResponse).toList();
    }
}
