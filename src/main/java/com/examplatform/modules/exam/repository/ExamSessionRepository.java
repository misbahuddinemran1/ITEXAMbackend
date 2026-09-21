package com.examplatform.modules.exam.repository;

import com.examplatform.modules.exam.entity.ExamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ExamSessionRepository extends JpaRepository<ExamSession, String> {



    Optional<ExamSession> findByIdAndUserId(String id, String userId);

    List<ExamSession> findByUserIdAndSessionTypeOrderByCreatedAtDesc(
            String userId, ExamSession.SessionType sessionType);

    long countByUserIdAndSessionType(String userId, ExamSession.SessionType sessionType);

    long countByCreatedAtAfter(LocalDateTime dateTime);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    long countByUserId(String userId);
    long countByUserIdAndStatusIn(String userId, List<ExamSession.Status> statuses);

    List<ExamSession> findTop5ByOrderByCreatedAtDesc();
    // এই methods যোগ করুন existing repository এ:

    List<ExamSession> findByUserIdOrderByCreatedAtDesc(String userId);

    @Query("SELECT DISTINCT es.userId FROM ExamSession es")
    List<String> findAllDistinctUserIds();

    @Query("SELECT COUNT(es) FROM ExamSession es WHERE es.userId = ?1 AND es.status = 'COMPLETED'")
    int countCompletedExams(String userId);

    @Query("SELECT AVG(es.percentage) FROM ExamSession es WHERE es.userId = ?1")
    Double getAveragePercentage(String userId);


    @Query("SELECT DISTINCT es.userId FROM ExamSession es WHERE es.specialExamId = ?1")
    java.util.List<String> findDistinctUserIdsBySpecialExamId(String specialExamId);
}
