package com.examplatform.modules.challenge.repository;

import com.examplatform.modules.challenge.entity.ChallengeQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeQuestionRepository extends JpaRepository<ChallengeQuestion, String> {
    List<ChallengeQuestion> findByChallengeIdOrderByOrderIndexAsc(String challengeId);
}
