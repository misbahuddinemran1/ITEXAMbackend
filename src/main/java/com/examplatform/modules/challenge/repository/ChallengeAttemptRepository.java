package com.examplatform.modules.challenge.repository;

import com.examplatform.modules.challenge.entity.ChallengeAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, String> {
    List<ChallengeAttempt> findByChallengeIdAndUserId(String challengeId, String userId);
    Optional<ChallengeAttempt> findByChallengeIdAndUserIdAndMcqId(String challengeId, String userId, String mcqId);
    long countByChallengeIdAndUserId(String challengeId, String userId);
}
