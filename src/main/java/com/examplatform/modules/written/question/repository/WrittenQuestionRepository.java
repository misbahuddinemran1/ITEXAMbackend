package com.examplatform.modules.written.question.repository;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrittenQuestionRepository extends JpaRepository<WrittenQuestion, String> {

    List<WrittenQuestion> findByExamIdOrderByQuestionOrderAsc(String examId);

    long countByExamId(String examId);

    void deleteByExamId(String examId);

    // Added for Guide module (v1) — topic-wise practice content
    List<WrittenQuestion> findByTopicId(String topicId);

    List<WrittenQuestion> findByTopicIdAndIsBoardQuestionTrue(String topicId);
}
