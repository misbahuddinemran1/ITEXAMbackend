package com.examplatform.modules.examtype.repository;

import com.examplatform.modules.examtype.entity.ExamType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamTypeRepository
        extends JpaRepository<ExamType, String> {

    List<ExamType> findAllByIsActiveOrderByName(boolean isActive);

    List<ExamType> findAllByOrderByName();

    Optional<ExamType> findByCode(String code);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, String id);
}