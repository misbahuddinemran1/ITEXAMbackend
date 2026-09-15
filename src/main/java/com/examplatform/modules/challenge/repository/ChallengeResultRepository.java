package com.examplatform.modules.challenge.repository;

import com.examplatform.modules.challenge.entity.ChallengeResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeResultRepository extends JpaRepository<ChallengeResult, String> {
    Optional<ChallengeResult> findByChallengeId(String challengeId);
}
