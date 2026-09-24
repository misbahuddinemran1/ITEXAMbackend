package com.examplatform.modules.liveexam.repository;

import com.examplatform.modules.liveexam.entity.LiveQuestionAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LiveQuestionAttemptRepository extends JpaRepository<LiveQuestionAttempt, String> {

    List<LiveQuestionAttempt> findByUserId(String userId);

    List<LiveQuestionAttempt> findByUserIdAndQuestionId(String userId, String questionId);

    List<LiveQuestionAttempt> findBySessionId(String sessionId);

    long countByQuestionIdAndIsCorrectTrue(String questionId);

    long countByQuestionIdAndIsCorrectFalse(String questionId);

    long countByQuestionIdAndIsSkippedTrue(String questionId);

    @Query(value = """
            SELECT question_id AS questionId,
                   COUNT(*) FILTER (WHERE is_correct = true) AS correctCount,
                   COUNT(*) FILTER (WHERE is_correct = false AND is_skipped = false) AS wrongCount,
                   COUNT(*) FILTER (WHERE is_skipped = true) AS skipCount
            FROM live_question_attempts
            GROUP BY question_id
            HAVING COUNT(*) FILTER (WHERE is_skipped = false) >= :minAttempts
            ORDER BY (CAST(COUNT(*) FILTER (WHERE is_correct = true) AS decimal)
                      / NULLIF(COUNT(*) FILTER (WHERE is_skipped = false), 0)) ASC
            LIMIT :limit
            """, nativeQuery = true)
    List<QuestionAggregateProjection> findHardestQuestions(
            @Param("minAttempts") int minAttempts,
            @Param("limit") int limit);

    interface QuestionAggregateProjection {
        String getQuestionId();
        Long getCorrectCount();
        Long getWrongCount();
        Long getSkipCount();
    }
}
