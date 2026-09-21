
package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, String> {

    // Admin — সব exam list
    List<Exam> findAllByOrderByCreatedAtDesc();

    // Type অনুযায়ী
    List<Exam> findByExamTypeOrderByCreatedAtDesc(Exam.ExamType examType);

    // Published exams (student side)
    List<Exam> findByPublishStatusOrderByExamDateAsc(Exam.PublishStatus status);

    // আজকের published exams
    List<Exam> findByPublishStatusAndExamDate(
            Exam.PublishStatus status,
            LocalDate examDate
    );

    // Leaderboard (RELATIVE threshold): নির্দিষ্ট সময়ে কতগুলো published exam
    long countByPublishStatusAndExamDateBetween(
            Exam.PublishStatus status,
            LocalDate start,
            LocalDate end
    );

    // Exam code check
    Optional<Exam> findByExamCode(String examCode);

    boolean existsByExamCode(String examCode);

    // Published + specific type (student)
    List<Exam> findByPublishStatusAndExamTypeOrderByExamDateAsc(
            Exam.PublishStatus status,
            Exam.ExamType examType
    );
    
List<Exam> findByPublishStatus(Exam.PublishStatus publishStatus);
    // Admin stats
    long countByPublishStatus(Exam.PublishStatus status);

    long countByExamType(Exam.ExamType examType);

    // Date range query
    @Query("SELECT e FROM Exam e WHERE e.publishStatus = :status " +
            "AND e.examDate BETWEEN :startDate AND :endDate " +
            "ORDER BY e.examDate ASC")
    List<Exam> findPublishedExamsByDateRange(
            @Param("status") Exam.PublishStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
