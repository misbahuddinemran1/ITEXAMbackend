package com.examplatform.modules.written.submission.service;

import com.examplatform.modules.user.entity.User;
import com.examplatform.modules.user.repository.UserRepository;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionFileRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import com.examplatform.modules.written.submission.response.AdminSubmissionSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class AdminSubmissionQueryService {

    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionFileRepository submissionFileRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenExamRepository examRepository;
    private final UserRepository userRepository;

    public List<AdminSubmissionSummaryResponse> getSubmissionsForExam(String examId) {
        WrittenExam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        List<WrittenSubmission> submissions = submissionRepository.findByExamId(examId);

        return submissions.stream()
                .map(sub -> toSummary(sub, exam))
                .toList();
    }

    public AdminSubmissionSummaryResponse getSubmissionSummary(String submissionId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));
        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));
        return toSummary(submission, exam);
    }

    private AdminSubmissionSummaryResponse toSummary(WrittenSubmission submission, WrittenExam exam) {
        User student = userRepository.findById(submission.getUserId())
                .orElseThrow(() -> new NoSuchElementException("Student not found: " + submission.getUserId()));

        List<WrittenSubmissionFile> files = submissionFileRepository
                .findBySubmissionIdOrderByPageNumberAsc(submission.getId());

        String fileType = files.isEmpty() ? null : files.get(0).getFileType().name();
        boolean hasTranscript = transcriptRepository.existsBySubmissionId(submission.getId());

        return AdminSubmissionSummaryResponse.builder()
                .submissionId(submission.getId())
                .examId(exam.getId())
                .examTitle(exam.getTitle())
                .studentId(student.getId())
                .studentName(student.getFullName())
                .studentEducationLevel(student.getEducationLevel() != null ? student.getEducationLevel().name() : null)
                .studentInstitution(student.getInstitutionName())
                .status(submission.getStatus().name())
                .fileType(fileType)
                .cycleNumber(submission.getCycleNumber())
                .attemptNumber(submission.getAttemptNumber())
                .submittedAt(submission.getSubmittedAt())
                .hasTranscript(hasTranscript)
                .build();
    }
}
