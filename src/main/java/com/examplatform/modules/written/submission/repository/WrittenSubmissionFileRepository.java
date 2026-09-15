package com.examplatform.modules.written.submission.repository;

import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrittenSubmissionFileRepository extends JpaRepository<WrittenSubmissionFile, String> {

    List<WrittenSubmissionFile> findBySubmissionIdOrderByPageNumberAsc(String submissionId);

    long countBySubmissionId(String submissionId);

    void deleteBySubmissionId(String submissionId);
}