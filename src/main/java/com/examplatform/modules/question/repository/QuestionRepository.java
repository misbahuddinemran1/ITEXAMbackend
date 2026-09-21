package com.examplatform.modules.question.repository;

import com.examplatform.modules.question.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuestionRepository
        extends JpaRepository<Question, String> {

    @Query("SELECT q FROM Question q WHERE " +
            "(:status IS NULL OR q.status = :status) AND " +
            "(:subjectId IS NULL OR q.subject.id = :subjectId) AND " +
            "(:chapterId IS NULL OR q.chapter.id = :chapterId) AND " +
            "(:topicId IS NULL OR q.topic.id = :topicId) AND " +
            "(:difficulty IS NULL OR q.difficultyLevel = :difficulty)")
    Page<Question> findWithFilters(
            @Param("status") Question.QuestionStatus status,
            @Param("subjectId") String subjectId,
            @Param("chapterId") String chapterId,
            @Param("topicId") String topicId,
            @Param("difficulty") Integer difficulty,
            Pageable pageable);

    Optional<Question> findByContentHash(String contentHash);

    // Dashboard / Review Queue: নির্দিষ্ট status এ কতগুলো প্রশ্ন আছে
    long countByStatus(Question.QuestionStatus status);

    boolean existsByContentHash(String contentHash);
}