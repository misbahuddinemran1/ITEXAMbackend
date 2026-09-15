package com.examplatform.modules.guide.repository;

import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuidePracticeMcqRepository extends JpaRepository<GuidePracticeMcq, String> {

    List<GuidePracticeMcq> findByTopicIdOrderBySortOrderAsc(String topicId);

    List<GuidePracticeMcq> findByTopicIdAndIsBoardQuestionTrueOrderBySortOrderAsc(String topicId);

    List<GuidePracticeMcq> findAllByOrderBySortOrderAsc();
        List<GuidePracticeMcq> findByTopicId(String topicId);

    List<GuidePracticeMcq> findByTopic_Chapter_Id(String chapterId);
}
