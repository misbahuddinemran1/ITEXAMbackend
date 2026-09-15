package com.examplatform.modules.challenge.repository;

import com.examplatform.modules.challenge.entity.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, String> {

    @Query("SELECT c FROM Challenge c WHERE c.mode = 'RANDOM' AND c.status = 'PENDING' " +
           "AND c.opponent IS NULL AND c.creator.id <> :userId " +
           "AND c.chapter.id = :chapterId " +
           "AND (:topicId IS NULL AND c.topic IS NULL OR c.topic.id = :topicId) " +
           "AND c.questionCount = :questionCount " +
           "ORDER BY c.createdAt ASC")
    List<Challenge> findWaitingRandomMatch(
            @Param("userId") String userId,
            @Param("chapterId") String chapterId,
            @Param("topicId") String topicId,
            @Param("questionCount") int questionCount
    );

    @Query("SELECT c FROM Challenge c WHERE (c.creator.id = :userId OR c.opponent.id = :userId) " +
           "AND (:status IS NULL OR c.status = :status) ORDER BY c.createdAt DESC")
    List<Challenge> findMyChallenges(@Param("userId") String userId, @Param("status") Challenge.Status status);

        Optional<Challenge> findByIdAndStatus(String id, Challenge.Status status);

    @Query("SELECT c FROM Challenge c WHERE c.mode = 'FRIEND' AND c.status = 'PENDING' " +
           "AND ((c.creator.id = :userA AND c.opponent.id = :userB) " +
           "OR (c.creator.id = :userB AND c.opponent.id = :userA))")
    Optional<Challenge> findPendingBetween(@Param("userA") String userA, @Param("userB") String userB);
}
