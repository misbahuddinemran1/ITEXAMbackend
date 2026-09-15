package com.examplatform.modules.guide.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.guide.dto.GuideContentAdminRequest;
import com.examplatform.modules.guide.entity.GuideContent;
import com.examplatform.modules.guide.repository.GuideContentRepository;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuideContentAdminService {

    private final GuideContentRepository guideContentRepository;
    private final TopicRepository topicRepository;

    public List<GuideContent> getAll() {
        return guideContentRepository.findAllByOrderBySortOrderAsc();
    }

    public GuideContent getById(String id) {
        return guideContentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guide content not found: " + id));
    }

    @Transactional
    public GuideContent create(GuideContentAdminRequest req) {
        if (guideContentRepository.findByTopicId(req.getTopicId()).isPresent()) {
            throw new IllegalStateException("Guide content already exists for this topic. Use update instead.");
        }

        Topic topic = topicRepository.findById(req.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + req.getTopicId()));

        GuideContent content = GuideContent.builder()
                .topic(topic)
                .title(req.getTitle())
                .bodyHtml(req.getBodyHtml())
                .pdfUrl(req.getPdfUrl())
                .sortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0)
                .status(GuideContent.GuideStatus.DRAFT)
                .build();

        return guideContentRepository.save(content);
    }

    @Transactional
    public GuideContent update(String id, GuideContentAdminRequest req) {
        GuideContent content = getById(id);

        content.setTitle(req.getTitle());
        content.setBodyHtml(req.getBodyHtml());
        content.setPdfUrl(req.getPdfUrl());
        if (req.getSortOrder() != null) {
            content.setSortOrder(req.getSortOrder());
        }

        return guideContentRepository.save(content);
    }

    @Transactional
    public GuideContent publish(String id) {
        GuideContent content = getById(id);
        content.setStatus(GuideContent.GuideStatus.PUBLISHED);
        content.setPublishedAt(LocalDateTime.now());
        return guideContentRepository.save(content);
    }

    @Transactional
    public GuideContent archive(String id) {
        GuideContent content = getById(id);
        content.setStatus(GuideContent.GuideStatus.ARCHIVED);
        return guideContentRepository.save(content);
    }

    @Transactional
    public void delete(String id) {
        GuideContent content = getById(id);
        guideContentRepository.delete(content);
    }
}
