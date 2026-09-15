package com.examplatform.modules.guide.repository;

import com.examplatform.modules.guide.entity.GuidePracticeCq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuidePracticeCqRepository extends JpaRepository<GuidePracticeCq, String> {

    List<GuidePracticeCq> findByTopicIdOrderBySortOrderAsc(String topicId);

    List<GuidePracticeCq> findByTopicIdAndIsBoardQuestionTrueOrderBySortOrderAsc(String topicId);

    List<GuidePracticeCq> findAllByOrderBySortOrderAsc();
}
