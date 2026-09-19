package com.examplatform.modules.written.evaluation.ai.controller;

import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptBulkRequest;
import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptEntryRequest;
import com.examplatform.modules.written.evaluation.ai.service.TranscriptionOrchestrationService;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/admin/written/submissions/{submissionId}/transcript")
@RequiredArgsConstructor
public class AdminTranscriptionController {

    private final TranscriptionOrchestrationService transcriptionOrchestrationService;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenQuestionRepository questionRepository;
    private final WrittenExamRepository examRepository;

    @PostMapping("/ai")
    public String triggerAiTranscription(@PathVariable String submissionId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(submission.getExamId());

        Map<String, List<Integer>> partsToTranscribe = new HashMap<>();
        boolean anyAiPart = false;
        for (WrittenQuestion q : questions) {
            List<Integer> aiParts = resolveAiPartOrders(exam, q.getPartCount());
            if (!aiParts.isEmpty()) anyAiPart = true;
            partsToTranscribe.put(q.getId(), aiParts);
        }

        if (!anyAiPart) {
            return "No AI-mode sub-questions configured for this exam (evaluationMode=MANUAL). Nothing to transcribe.";
        }

        transcriptionOrchestrationService.ensureTranscribed(submission.getExamId(), submissionId, partsToTranscribe);
        return "Transcription completed for submission: " + submissionId;
    }

    private List<Integer> resolveAiPartOrders(WrittenExam exam, int partCount) {
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

    @GetMapping
    public List<Map<String, Object>> getTranscripts(@PathVariable String submissionId) {
        if (!submissionRepository.existsById(submissionId)) {
            throw new NoSuchElementException("Submission not found: " + submissionId);
        }

        List<WrittenSubmissionTranscript> transcripts = transcriptRepository.findBySubmissionId(submissionId);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WrittenSubmissionTranscript t : transcripts) {
            Map<String, Object> item = new HashMap<>();
            item.put("questionId", t.getQuestion().getId());
            item.put("questionOrder", t.getQuestion().getQuestionOrder());
            item.put("partOrder", t.getPartOrder());
            item.put("transcribedText", t.getTranscribedText());
            result.add(item);
        }
        return result;
    }

    @PostMapping("/manual")
    public String submitManualTranscript(@PathVariable String submissionId,
                                          @Valid @RequestBody ManualTranscriptBulkRequest request) {

        if (!submissionRepository.existsById(submissionId)) {
            throw new NoSuchElementException("Submission not found: " + submissionId);
        }

        for (ManualTranscriptEntryRequest entry : request.getEntries()) {
            WrittenQuestion question = questionRepository.findById(entry.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + entry.getQuestionId()));

            int partOrder = entry.getPartOrder();

            WrittenSubmissionTranscript transcript = transcriptRepository
                    .findBySubmissionIdAndQuestionIdAndPartOrder(submissionId, question.getId(), partOrder)
                    .orElse(WrittenSubmissionTranscript.builder()
                            .submissionId(submissionId)
                            .question(question)
                            .partOrder(partOrder)
                            .build());

            transcript.setTranscribedText(entry.getTranscribedText());
            transcriptRepository.save(transcript);
        }

        return "Manual transcript saved for submission: " + submissionId;
    }
}
