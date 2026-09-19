package com.examplatform.modules.written.evaluation.service;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
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

    @Transactional
    public void runPredictedMatching(String submissionId, String adminId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        if (exam.getEvaluationMode() == com.examplatform.modules.written.exam.enums.EvaluationMode.MANUAL) {
            throw new IllegalStateException("This exam has evaluationMode=MANUAL — no AI-mode parts. " +
                    "Use manual evaluation instead.");
        }

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(submission.getExamId());
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions found for exam: " + submission.getExamId());
        }

        List<WrittenSubmissionTranscript> transcripts = transcriptRepository.findBySubmissionId(submissionId);
        Map<String, String> transcriptTextByKey = new HashMap<>();
        for (WrittenSubmissionTranscript t : transcripts) {
            transcriptTextByKey.put(t.getQuestion().getId() + ":" + t.getPartOrder(), t.getTranscribedText());
        }

        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> WrittenEvaluation.builder()
                        .submission(submission)
                        .evaluationMode(exam.getEvaluationMode())
                        .status(EvaluationStatus.PROCESSING)
                        .build());

        List<String> missingTranscripts = new ArrayList<>();
        boolean anyAiPart = false;

        for (WrittenQuestion question : questions) {
            List<Integer> aiParts = resolveAiPartOrders(exam, question.getPartCount());
            for (int partOrder : aiParts) {
                anyAiPart = true;
                String key = question.getId() + ":" + partOrder;
                String transcribedText = transcriptTextByKey.get(key);

                if (transcribedText == null || transcribedText.isBlank()) {
                    missingTranscripts.add(question.getQuestionOrder() + "-" + partOrder);
                    continue;
                }

                processDetail(evaluation, question, partOrder, transcribedText);
            }
        }

        if (!anyAiPart) {
            throw new IllegalStateException("This exam has no AI-mode sub-questions configured. " +
                    "Use manual evaluation instead.");
        }

        if (!missingTranscripts.isEmpty()) {
            throw new IllegalStateException("Missing transcripts for question-subquestions: " + missingTranscripts
                    + ". Please run transcription first (/transcript/ai or /transcript/manual).");
        }

        evaluation.setStatus(EvaluationStatus.PENDING_REVIEW);
        evaluation.setEvaluatedByAdminId(adminId);
        evaluationRepository.save(evaluation);

        submission.setStatus(SubmissionStatus.UNDER_REVIEW);
        submissionRepository.save(submission);
    }

    private void processDetail(WrittenEvaluation evaluation, WrittenQuestion question,
                                int partOrder, String transcribedText) {

        String modelAnswer = question.getPartModelAnswer(partOrder);
        String aiAnswer = question.getPartAiAnswer(partOrder);
        BigDecimal maxMark = question.getPartMaxMark(partOrder);

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

        if (evaluation.getId() == null) {
            evaluationRepository.save(evaluation);
        }

        WrittenEvaluationDetail detail = evaluationDetailRepository.findByEvaluationId(evaluation.getId()).stream()
                .filter(d -> d.getQuestion().getId().equals(question.getId()) && d.getPartOrder() == partOrder)
                .findFirst()
                .orElse(WrittenEvaluationDetail.builder()
                        .evaluation(evaluation)
                        .question(question)
                        .partOrder(partOrder)
                        .maxMark(maxMark)
                        .build());

        detail.setMatchScoreManual(matchScoreManual);
        detail.setPredictedMarkManual(predictedMarkManual);
        detail.setMatchScoreAi(matchScoreAi);
        detail.setPredictedMarkAi(predictedMarkAi);
        detail.setMaxMark(maxMark);

        evaluationDetailRepository.save(detail);
    }

    static List<Integer> resolveAiPartOrders(WrittenExam exam, int partCount) {
        List<Integer> parts = new ArrayList<>();

        switch (exam.getEvaluationMode()) {
            case AI -> {
                for (int i = 1; i <= partCount; i++) parts.add(i);
            }
            case MANUAL -> { /* no AI parts */ }
            case HYBRID -> {
                if (exam.getAiPartOrders() != null) {
                    for (int i = 1; i <= partCount; i++) {
                        if (exam.getAiPartOrders().contains(i)) parts.add(i);
                    }
                }
            }
        }

        return parts;
    }
}
