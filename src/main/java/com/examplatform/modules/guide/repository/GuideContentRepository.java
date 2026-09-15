package com.examplatform.modules.guide.repository;

import com.examplatform.modules.guide.entity.GuideContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GuideContentRepository extends JpaRepository<GuideContent, String> {

    Optional<GuideContent> findByTopicId(String topicId);

    Optional<GuideContent> findByTopicIdAndStatus(String topicId, GuideContent.GuideStatus status);

    List<GuideContent> findByStatus(GuideContent.GuideStatus status);

    List<GuideContent> findAllByOrderBySortOrderAsc();
}
