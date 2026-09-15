package com.examplatform.modules.written.submission.repository;

import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrittenSubmissionRepository extends JpaRepository<WrittenSubmission, String> {

    boolean existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
            String examId, String userId, Integer cycleNumber);

    boolean existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalseAndStatusIn(
            String examId, String userId, Integer cycleNumber, List<SubmissionStatus> statuses);

    /**
     * NOTE: returns a List, not Optional/single — in practice there should only ever be one
     * non-practice submission per exam+cycle+user, but legacy crash-created duplicates can exist,
     * and a derived Optional-returning query throws IncorrectResultSizeDataAccessException the
     * moment more than one row matches. The service layer picks the right one from the list.
     */
    List<WrittenSubmission> findAllByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
            String examId, String userId, Integer cycleNumber);

    List<WrittenSubmission> findByExamIdAndUserIdAndIsPracticeModeTrueOrderByAttemptNumberDesc(
            String examId, String userId);

    long countByExamIdAndUserIdAndIsPracticeModeTrue(String examId, String userId);

    List<WrittenSubmission> findByExamIdAndStatus(String examId, SubmissionStatus status);

    List<WrittenSubmission> findByUserIdOrderByCreatedAtDesc(String userId);

    List<WrittenSubmission> findByExamId(String examId);
}
