package com.examplatform.modules.written.submission.repository;

import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WrittenSubmissionTranscriptRepository extends JpaRepository<WrittenSubmissionTranscript, String> {

    List<WrittenSubmissionTranscript> findBySubmissionId(String submissionId);

    Optional<WrittenSubmissionTranscript> findBySubmissionIdAndQuestionIdAndPartOrder(
            String submissionId, String questionId, Integer partOrder);

    boolean existsBySubmissionId(String submissionId);
}
