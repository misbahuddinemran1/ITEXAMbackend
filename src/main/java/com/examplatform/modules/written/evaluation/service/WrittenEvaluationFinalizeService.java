package com.examplatform.modules.written.evaluation.service;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.manual.request.PartMarkRequest;
import com.examplatform.modules.written.evaluation.mapper.WrittenEvaluationMapper;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.settings.repository.WrittenSettingsRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.exam.entity.UserNotification;
import com.examplatform.modules.exam.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

/**
 * Mode-aware finalization of an evaluation (works for MANUAL, AI, and HYBRID exams).
 */
@Service
@RequiredArgsConstructor
public class WrittenEvaluationFinalizeService {

    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository detailRepository;
    private final WrittenEvaluationMapper evaluationMapper;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenQuestionRepository questionRepository;
    private final WrittenExamRepository examRepository;
    private final WrittenSettingsRepository settingsRepository;
    private final NotificationService notificationService;

    private static final String SETTINGS_ID = "default";

    @Transactional
    public EvaluationResponse finalizeEvaluation(String submissionId, ManualEvaluationRequest request, String adminId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        // Finalizing is allowed for SUBMITTED, UNDER_REVIEW, or already-COMPLETED submissions —
        // the last case lets an admin correct a mistake and re-finalize later. It's only
        // blocked before the student has actually submitted (NOT_STARTED / IN_PROGRESS).
        if (submission.getStatus() == SubmissionStatus.NOT_STARTED
                || submission.getStatus() == SubmissionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Submission has not been submitted yet — cannot finalize evaluation");
        }

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> WrittenEvaluation.builder()
                        .submission(submission)
                        .evaluationMode(exam.getEvaluationMode())
                        .status(EvaluationStatus.PENDING)
                        .build());

        evaluation.setEvaluationMode(exam.getEvaluationMode());

        WrittenEvaluation savedEvaluation = evaluationRepository.save(evaluation);

        BigDecimal totalMark = BigDecimal.ZERO;

        for (PartMarkRequest partMark : request.getPartMarks()) {
            WrittenQuestion question = questionRepository.findById(partMark.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + partMark.getQuestionId()));

            QuestionPart part = QuestionPart.valueOf(partMark.getPart());
            BigDecimal maxMark = resolveMaxMark(question, part);

            if (partMark.getObtainedMark().compareTo(maxMark) > 0) {
                throw new IllegalArgumentException("obtainedMark exceeds maxMark for question "
                        + question.getId() + " part " + part);
            }
            if (partMark.getObtainedMark().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("obtainedMark cannot be negative");
            }

            WrittenEvaluationDetail detail = detailRepository.findByEvaluationId(savedEvaluation.getId()).stream()
                    .filter(d -> d.getQuestion().getId().equals(question.getId()) && d.getPart() == part)
                    .findFirst()
                    .orElse(WrittenEvaluationDetail.builder()
                            .evaluation(savedEvaluation)
                            .question(question)
                            .part(part)
                            .build());

            detail.setObtainedMark(partMark.getObtainedMark());
            detail.setMaxMark(maxMark);
            if (partMark.getFeedback() != null) {
                detail.setFeedback(partMark.getFeedback());
            }

            detailRepository.save(detail);
            totalMark = totalMark.add(partMark.getObtainedMark());
        }

        savedEvaluation.setTotalMark(totalMark);
        savedEvaluation.setStatus(EvaluationStatus.COMPLETED);
        savedEvaluation.setEvaluatedByAdminId(adminId);
        savedEvaluation.setEvaluatedAt(LocalDateTime.now());

        String resultPublishMode = settingsRepository.findById(SETTINGS_ID)
                .map(s -> s.getResultPublishMode())
                .orElse("MANUAL");
        boolean instantPublish = "INSTANT".equalsIgnoreCase(resultPublishMode);
        if (instantPublish) {
            savedEvaluation.setResultPublished(true);
        }

        evaluationRepository.save(savedEvaluation);

        submission.setTotalObtainedMark(totalMark);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submissionRepository.save(submission);

        // INSTANT মোডে finalize হওয়ার সাথে সাথেই ফলাফল প্রকাশিত হয়ে যায়, তাই তখনই notify করা হচ্ছে
        if (instantPublish) {
            sendResultNotification(submission, exam);
        }

        return evaluationMapper.toResponse(savedEvaluation, detailRepository.findByEvaluationId(savedEvaluation.getId()));
    }

    @Transactional
    public int publishAllResultsForExam(String examId) {
        java.util.List<WrittenSubmission> submissions = submissionRepository.findByExamId(examId);

        WrittenExam exam = examRepository.findById(examId).orElse(null);

        int publishedCount = 0;
        for (WrittenSubmission submission : submissions) {
            var evaluationOpt = evaluationRepository.findBySubmissionId(submission.getId());
            if (evaluationOpt.isEmpty()) continue;

            WrittenEvaluation evaluation = evaluationOpt.get();
            if (evaluation.getStatus() == EvaluationStatus.COMPLETED && !evaluation.isResultPublished()) {
                evaluation.setResultPublished(true);
                evaluationRepository.save(evaluation);
                publishedCount++;

                if (exam != null) {
                    sendResultNotification(submission, exam);
                }
            }
        }
        return publishedCount;
    }

    private BigDecimal resolveMaxMark(WrittenQuestion question, QuestionPart part) {
        return switch (part) {
            case A -> question.getPartAMaxMark();
            case B -> question.getPartBMaxMark();
            case C -> question.getPartCMaxMark();
            case D -> question.getPartDMaxMark();
        };
    }

    @Transactional
    public EvaluationResponse publishResult(String submissionId) {
        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found for submission: " + submissionId));

        if (evaluation.getStatus() != EvaluationStatus.COMPLETED) {
            throw new IllegalStateException("Evaluation must be finalized (COMPLETED) before it can be published");
        }

        evaluation.setResultPublished(true);
        evaluationRepository.save(evaluation);

        WrittenSubmission submission = evaluation.getSubmission();
        WrittenExam exam = examRepository.findById(submission.getExamId()).orElse(null);
        if (exam != null) {
            sendResultNotification(submission, exam);
        }

        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }

    /**
     * Written exam এর ফলাফল প্রকাশ হলে ছাত্রকে notification পাঠানো —
     * সবখানে একই generic NotificationService ব্যবহার করা হচ্ছে (MCQ/Live exam এও এটাই ব্যবহৃত হয়)
     */
    private void sendResultNotification(WrittenSubmission submission, WrittenExam exam) {
        try {
            notificationService.sendNotification(
                    submission.getUserId(),
                    UserNotification.NotificationType.EXAM_RESULT,
                    "রেজাল্ট প্রকাশিত হয়েছে",
                    "\"" + exam.getTitle() + "\" পরীক্ষার ফলাফল প্রকাশিত হয়েছে। এখনই দেখুন।"
            );
        } catch (Exception e) {
            // Notification পাঠাতে ব্যর্থ হলেও publish প্রক্রিয়া যেন আটকে না যায়
        }
    }
}
