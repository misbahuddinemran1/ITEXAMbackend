package com.examplatform.modules.written.evaluation.repository;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WrittenEvaluationRepository extends JpaRepository<WrittenEvaluation, String> {

    Optional<WrittenEvaluation> findBySubmissionId(String submissionId);

    boolean existsBySubmissionId(String submissionId);

    /**
     * Leaderboard query for a written exam: only evaluations whose result has been
     * published to students (resultPublished=true, status=COMPLETED), and excluding
     * practice-mode submissions. Cycle-agnostic — a student can only ever have one
     * lifetime (non-practice) attempt for a given exam regardless of how many times
     * the exam has been reopened, so all cycles are merged into a single leaderboard.
     * Ordered highest mark first.
     */
    @Query("SELECT e FROM WrittenEvaluation e " +
           "WHERE e.submission.examId = :examId " +
           "AND e.submission.isPracticeMode = false " +
           "AND e.resultPublished = true " +
           "AND e.status = com.examplatform.modules.written.evaluation.enums.EvaluationStatus.COMPLETED " +
           "ORDER BY e.totalMark DESC")
    List<WrittenEvaluation> findLeaderboardByExamId(@Param("examId") String examId);
}
