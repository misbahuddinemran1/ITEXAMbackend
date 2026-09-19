package com.examplatform.modules.written.question.repository;

import com.examplatform.modules.written.question.entity.WrittenQuestionPart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrittenQuestionPartRepository extends JpaRepository<WrittenQuestionPart, String> {

    List<WrittenQuestionPart> findByQuestionIdOrderByPartOrderAsc(String questionId);

    Optional<WrittenQuestionPart> findByQuestionIdAndPartOrder(String questionId, Integer partOrder);

    void deleteByQuestionId(String questionId);
}
