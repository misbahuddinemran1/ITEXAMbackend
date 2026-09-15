package com.examplatform.modules.doubt.repository;

import com.examplatform.modules.doubt.entity.DoubtAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoubtAnswerRepository extends JpaRepository<DoubtAnswer, String> {
    Optional<DoubtAnswer> findByDoubtQuestionId(String doubtQuestionId);
}
