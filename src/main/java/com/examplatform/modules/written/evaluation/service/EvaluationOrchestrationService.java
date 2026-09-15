package com.examplatform.modules.written.evaluation.service;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.PartEvaluationMode;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EvaluationOrchestrationService {

    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenExamRepository examRepository;
    private final WrittenQuestionRepository questionRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository evaluationDetailRepository;
    private final AnswerMatchingService answerMatchingService;

    /**
     * Runs predicted-mark matching for every AI-designated (question, part) pair
     * in this submission's exam. Requires transcripts to already exist
     * (admin must trigger /transcript/ai or /transcript/manual first).
     * Saves predictedMarkManual/Ai + matchScoreManual/Ai per detail row.
     * Does NOT touch obtainedMark/totalMark — those are set during final admin approval.
     */
    @Transactional
    public void runPredictedMatching(String submissionId, String adminId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        List<QuestionPart> aiParts = resolveAiParts(exam);
        if (aiParts.isEmpty()) {
            throw new IllegalStateException("This exam has no AI-mode parts (evaluationMode=MANUAL). " +
                    "Use manual evaluation instead.");
        }

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(submission.getExamId());
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions found for exam: " + submission.getExamId());
        }

        List<WrittenSubmissionTranscript> transcripts = transcriptRepository.findBySubmissionId(submissionId);
        Map<String, String> transcriptTextByKey = new HashMap<>();
        for (WrittenSubmissionTranscript t : transcripts) {
            transcriptTextByKey.put(t.getQuestion().getId() + ":" + t.getPart().name(), t.getTranscribedText());
        }

        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> WrittenEvaluation.builder()
                        .submission(submission)
                        .evaluationMode(exam.getEvaluationMode())
                        .status(EvaluationStatus.PROCESSING)
                        .build());

        List<String> missingTranscripts = new ArrayList<>();

        for (WrittenQuestion question : questions) {
            for (QuestionPart part : aiParts) {
                String key = question.getId() + ":" + part.name();
                String transcribedText = transcriptTextByKey.get(key);

                if (transcribedText == null || transcribedText.isBlank()) {
                    missingTranscripts.add(question.getQuestionOrder() + "-" + part.name());
                    continue;
                }

                processDetail(evaluation, question, part, transcribedText);
            }
        }

        if (!missingTranscripts.isEmpty()) {
            throw new IllegalStateException("Missing transcripts for question-parts: " + missingTranscripts
                    + ". Please run transcription first (/transcript/ai or /transcript/manual).");
        }

        evaluation.setStatus(EvaluationStatus.PENDING_REVIEW);
        evaluation.setEvaluatedByAdminId(adminId);
        evaluationRepository.save(evaluation);

        submission.setStatus(SubmissionStatus.UNDER_REVIEW);
        submissionRepository.save(submission);
    }

    private void processDetail(WrittenEvaluation evaluation, WrittenQuestion question,
                                QuestionPart part, String transcribedText) {

        String modelAnswer = getModelAnswer(question, part);
        String aiAnswer = getAiAnswer(question, part);
        BigDecimal maxMark = getMaxMark(question, part);

        BigDecimal matchScoreManual = BigDecimal.ZERO;
        BigDecimal predictedMarkManual = BigDecimal.ZERO;
        if (modelAnswer != null && !modelAnswer.isBlank()) {
            matchScoreManual = answerMatchingService.calculateSimilarity(transcribedText, modelAnswer);
            predictedMarkManual = answerMatchingService.calculatePredictedMark(matchScoreManual, maxMark);
        }

        BigDecimal matchScoreAi = BigDecimal.ZERO;
        BigDecimal predictedMarkAi = BigDecimal.ZERO;
        if (aiAnswer != null && !aiAnswer.isBlank()) {
            matchScoreAi = answerMatchingService.calculateSimilarity(transcribedText, aiAnswer);
            predictedMarkAi = answerMatchingService.calculatePredictedMark(matchScoreAi, maxMark);
        }

        // evaluation must be persisted first (has an id) before details can reference it
        if (evaluation.getId() == null) {
            evaluationRepository.save(evaluation);
        }

        WrittenEvaluationDetail detail = evaluationDetailRepository.findByEvaluationId(evaluation.getId()).stream()
                .filter(d -> d.getQuestion().getId().equals(question.getId()) && d.getPart() == part)
                .findFirst()
                .orElse(WrittenEvaluationDetail.builder()
                        .evaluation(evaluation)
                        .question(question)
                        .part(part)
                        .maxMark(maxMark)
                        .build());

        detail.setMatchScoreManual(matchScoreManual);
        detail.setPredictedMarkManual(predictedMarkManual);
        detail.setMatchScoreAi(matchScoreAi);
        detail.setPredictedMarkAi(predictedMarkAi);
        detail.setMaxMark(maxMark);

        evaluationDetailRepository.save(detail);
    }

    private String getModelAnswer(WrittenQuestion q, QuestionPart part) {
        return switch (part) {
            case A -> q.getPartAModelAnswer();
            case B -> q.getPartBModelAnswer();
            case C -> q.getPartCModelAnswer();
            case D -> q.getPartDModelAnswer();
        };
    }

    private String getAiAnswer(WrittenQuestion q, QuestionPart part) {
        return switch (part) {
            case A -> q.getPartAAiAnswer();
            case B -> q.getPartBAiAnswer();
            case C -> q.getPartCAiAnswer();
            case D -> q.getPartDAiAnswer();
        };
    }

    private BigDecimal getMaxMark(WrittenQuestion q, QuestionPart part) {
        return switch (part) {
            case A -> q.getPartAMaxMark();
            case B -> q.getPartBMaxMark();
            case C -> q.getPartCMaxMark();
            case D -> q.getPartDMaxMark();
        };
    }

    /**
     * Determines which parts (A/B/C/D) are AI-mode for this exam.
     * Same logic as AdminTranscriptionController.resolveAiParts —
     * kept duplicated here to keep this service self-contained.
     */
    private List<QuestionPart> resolveAiParts(WrittenExam exam) {
        List<QuestionPart> parts = new ArrayList<>();

        switch (exam.getEvaluationMode()) {
            case AI -> parts.addAll(List.of(QuestionPart.A, QuestionPart.B, QuestionPart.C, QuestionPart.D));
            case MANUAL -> { /* no AI parts */ }
            case HYBRID -> {
                if (exam.getPartAMode() == PartEvaluationMode.AI) parts.add(QuestionPart.A);
                if (exam.getPartBMode() == PartEvaluationMode.AI) parts.add(QuestionPart.B);
                if (exam.getPartCMode() == PartEvaluationMode.AI) parts.add(QuestionPart.C);
                if (exam.getPartDMode() == PartEvaluationMode.AI) parts.add(QuestionPart.D);
            }
        }

        return parts;
    }
}
