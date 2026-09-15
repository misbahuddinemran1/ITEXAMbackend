package com.examplatform.modules.written.submission.service.impl;

import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.mapper.WrittenSubmissionMapper;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionFileRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import com.examplatform.modules.written.submission.request.StartExamRequest;
import com.examplatform.modules.written.submission.request.SubmitExamRequest;
import com.examplatform.modules.written.submission.request.SubmitTextAnswersRequest;
import com.examplatform.modules.written.submission.request.UploadSubmissionFileRequest;
import com.examplatform.modules.written.submission.response.SubmissionFileResponse;
import com.examplatform.modules.written.submission.response.SubmissionResponse;
import com.examplatform.modules.written.submission.service.WrittenSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenSubmissionServiceImpl implements WrittenSubmissionService {

    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionFileRepository fileRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenExamRepository examRepository;
    private final WrittenQuestionRepository questionRepository;
    private final WrittenSubmissionMapper submissionMapper;

    @Override
    @Transactional
    public SubmissionResponse startExam(String userId, StartExamRequest request) {
        WrittenExam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + request.getExamId()));

        if (request.isPracticeMode()) {
            return startPracticeAttempt(exam, userId);
        }
        return startLiveAttempt(exam, userId);
    }

    private SubmissionResponse startLiveAttempt(WrittenExam exam, String userId) {
        if (exam.getStatus() != ExamStatus.LIVE) {
            throw new IllegalStateException("Exam is not currently LIVE");
        }

        // If a submission already exists for this exam+cycle (any status), resume it instead of
        // creating a duplicate. This handles app crashes / accidental exits mid-exam gracefully —
        // the student just picks up where they left off with the same submissionId.
        // Legacy crash-created duplicate rows can exist, so we look at ALL of them and pick the
        // most relevant one: a finished attempt takes priority (blocks retry); otherwise resume
        // whichever IN_PROGRESS/NOT_STARTED row was created most recently.
        List<WrittenSubmission> existingList = submissionRepository
                .findAllByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalse(
                        exam.getId(), userId, exam.getCycleNumber());

        if (!existingList.isEmpty()) {
            WrittenSubmission finished = existingList.stream()
                    .filter(s -> s.getStatus() == SubmissionStatus.SUBMITTED
                            || s.getStatus() == SubmissionStatus.UNDER_REVIEW
                            || s.getStatus() == SubmissionStatus.COMPLETED)
                    .findFirst()
                    .orElse(null);

            if (finished != null) {
                throw new IllegalStateException("You have already attempted this exam in the current cycle");
            }

            WrittenSubmission mostRecent = existingList.stream()
                    .max(java.util.Comparator.comparing(WrittenSubmission::getCreatedAt))
                    .orElseThrow();
            return submissionMapper.toResponse(mostRecent, exam);
        }

        WrittenSubmission submission = WrittenSubmission.builder()
                .examId(exam.getId())
                .userId(userId)
                .cycleNumber(exam.getCycleNumber())
                .attemptNumber(1)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .isPracticeMode(false)
                .build();

        return submissionMapper.toResponse(submissionRepository.save(submission), exam);
    }



    private SubmissionResponse startPracticeAttempt(WrittenExam exam, String userId) {
        if (exam.getStatus() != ExamStatus.ENDED && exam.getStatus() != ExamStatus.ARCHIVED) {
            throw new IllegalStateException("Practice is only allowed on ended/archived exams");
        }

        if (Boolean.FALSE.equals(exam.getPracticeEnabled())) {
            throw new IllegalStateException("Practice is not enabled for this exam");
        }

        long previousPracticeCount = submissionRepository
                .countByExamIdAndUserIdAndIsPracticeModeTrue(exam.getId(), userId);

        WrittenSubmission submission = WrittenSubmission.builder()
                .examId(exam.getId())
                .userId(userId)
                .cycleNumber(exam.getCycleNumber())
                .attemptNumber((int) previousPracticeCount + 1)
                .status(SubmissionStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .isPracticeMode(true)
                .build();

        return submissionMapper.toResponse(submissionRepository.save(submission), exam);
    }

    @Override
    @Transactional
    public SubmissionFileResponse uploadFile(String submissionId, String userId, UploadSubmissionFileRequest request) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot upload file, submission is not in progress");
        }

        WrittenSubmissionFile file = WrittenSubmissionFile.builder()
                .submissionId(submissionId)
                .pageNumber(request.getPageNumber())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .build();

        return submissionMapper.toFileResponse(fileRepository.save(file));
    }

    /**
     * TEXT-mode answer submission — bypasses WrittenSubmissionFile entirely.
     * Each (questionId, part) has its own text box in the student app, so we can save
     * directly into WrittenSubmissionTranscript per part, with no AI transcription needed
     * (the student already typed the exact text). Calling this multiple times updates
     * existing entries rather than duplicating them, so students can edit answers
     * while still IN_PROGRESS.
     */
    @Override
    @Transactional
    public void submitTextAnswers(String submissionId, String userId, SubmitTextAnswersRequest request) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot submit answers, submission is not in progress");
        }

        for (SubmitTextAnswersRequest.TextAnswerEntry entry : request.getAnswers()) {
            WrittenQuestion question = questionRepository.findById(entry.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + entry.getQuestionId()));

            if (!question.getExamId().equals(submission.getExamId())) {
                throw new IllegalArgumentException("Question " + entry.getQuestionId()
                        + " does not belong to this submission's exam");
            }

            QuestionPart part = QuestionPart.valueOf(entry.getPart());

            WrittenSubmissionTranscript transcript = transcriptRepository
                    .findBySubmissionIdAndQuestionIdAndPart(submissionId, question.getId(), part)
                    .orElse(WrittenSubmissionTranscript.builder()
                            .submissionId(submissionId)
                            .question(question)
                            .part(part)
                            .build());

            transcript.setTranscribedText(entry.getAnswerText());
            transcriptRepository.save(transcript);
        }
    }

    @Override
    @Transactional
    public SubmissionResponse submitExam(String submissionId, String userId, SubmitExamRequest request) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);

        if (submission.getStatus() != SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Submission is not in progress");
        }

        long fileCount = fileRepository.countBySubmissionId(submissionId);
        long transcriptCount = transcriptRepository.findBySubmissionId(submissionId).size();

        if (fileCount == 0 && transcriptCount == 0) {
            throw new IllegalStateException("Cannot submit without uploading a file or answering at least one part");
        }

        submission.setStatus(SubmissionStatus.SUBMITTED);
        submission.setSubmittedAt(LocalDateTime.now());

        WrittenSubmission saved = submissionRepository.save(submission);
        WrittenExam exam = examRepository.findById(saved.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + saved.getExamId()));

        return submissionMapper.toResponse(saved, exam);
    }

    @Override
    public SubmissionResponse getSubmissionById(String submissionId, String userId) {
        WrittenSubmission submission = getOwnedSubmissionOrThrow(submissionId, userId);
        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        return submissionMapper.toResponse(submission, exam);
    }

    @Override
    public List<SubmissionFileResponse> getSubmissionFiles(String submissionId, String userId) {
        getOwnedSubmissionOrThrow(submissionId, userId);
        return fileRepository.findBySubmissionIdOrderByPageNumberAsc(submissionId).stream()
                .map(submissionMapper::toFileResponse)
                .toList();
    }

    @Override
    public List<SubmissionResponse> getMySubmissions(String userId) {
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(s -> {
                    WrittenExam exam = examRepository.findById(s.getExamId()).orElse(null);
                    return exam != null ? submissionMapper.toResponse(s, exam) : submissionMapper.toResponse(s);
                })
                .toList();
    }

    @Override
    public List<SubmissionResponse> getSubmissionsForExam(String examId) {
        return submissionRepository.findByExamIdAndStatus(examId, SubmissionStatus.SUBMITTED).stream()
                .map(submissionMapper::toResponse)
                .toList();
    }

    private WrittenSubmission getOwnedSubmissionOrThrow(String submissionId, String userId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        if (!submission.getUserId().equals(userId)) {
            throw new IllegalStateException("You do not have access to this submission");
        }
        return submission;
    }
}
