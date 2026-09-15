package com.examplatform.modules.written.evaluation.repository;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrittenEvaluationDetailRepository extends JpaRepository<WrittenEvaluationDetail, String> {

    List<WrittenEvaluationDetail> findByEvaluationId(String evaluationId);

    void deleteByEvaluationId(String evaluationId);
}