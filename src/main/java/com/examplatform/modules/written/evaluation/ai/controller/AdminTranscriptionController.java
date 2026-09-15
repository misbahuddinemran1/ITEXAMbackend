package com.examplatform.modules.written.evaluation.ai.controller;

import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptBulkRequest;
import com.examplatform.modules.written.evaluation.ai.request.ManualTranscriptEntryRequest;
import com.examplatform.modules.written.evaluation.ai.service.TranscriptionOrchestrationService;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.PartEvaluationMode;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
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

    /**
     * Admin explicitly triggers AI transcription for a PDF/IMAGE submission.
     * Transcribes ONLY the AI-mode question+parts for the exam this submission belongs to,
     * based on the exam's evaluationMode (MANUAL/AI/HYBRID) and per-part mode (for HYBRID).
     */
    @PostMapping("/ai")
    public String triggerAiTranscription(@PathVariable String submissionId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        WrittenExam exam = examRepository.findById(submission.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + submission.getExamId()));

        List<QuestionPart> aiParts = resolveAiParts(exam);

        if (aiParts.isEmpty()) {
            return "No AI-mode parts configured for this exam (evaluationMode=MANUAL). Nothing to transcribe.";
        }

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(submission.getExamId());

        Map<String, List<QuestionPart>> partsToTranscribe = new HashMap<>();
        for (WrittenQuestion q : questions) {
            partsToTranscribe.put(q.getId(), aiParts);
        }

        transcriptionOrchestrationService.ensureTranscribed(submission.getExamId(), submissionId, partsToTranscribe);
        return "Transcription completed for submission: " + submissionId + " (parts: " + aiParts + ")";
    }

    /**
     * Determines which parts (A/B/C/D) require AI transcription based on the exam's evaluationMode.
     * - evaluationMode = AI     -> all parts are AI-mode
     * - evaluationMode = MANUAL -> no parts need AI transcription (admin reads the PDF directly)
     * - evaluationMode = HYBRID -> only the parts individually set to AI (partAMode/B/C/D) are included
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

    /**
     * Returns all transcribed answers (AI or manually entered) for a submission,
     * so the admin can read the student's answers before giving manual marks.
     */
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
            item.put("part", t.getPart().name());
            item.put("transcribedText", t.getTranscribedText());
            result.add(item);
        }
        return result;
    }

    /**
     * Admin manually enters/pastes the transcribed text themselves (no AI call),
     * after extracting it some other way from the PDF/image.
     */
    @PostMapping("/manual")
    public String submitManualTranscript(@PathVariable String submissionId,
                                          @Valid @RequestBody ManualTranscriptBulkRequest request) {

        if (!submissionRepository.existsById(submissionId)) {
            throw new NoSuchElementException("Submission not found: " + submissionId);
        }

        for (ManualTranscriptEntryRequest entry : request.getEntries()) {
            WrittenQuestion question = questionRepository.findById(entry.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + entry.getQuestionId()));

            QuestionPart part = QuestionPart.valueOf(entry.getPart());

            WrittenSubmissionTranscript transcript = transcriptRepository
                    .findBySubmissionIdAndQuestionIdAndPart(submissionId, question.getId(), part)
                    .orElse(WrittenSubmissionTranscript.builder()
                            .submissionId(submissionId)
                            .question(question)
                            .part(part)
                            .build());

            transcript.setTranscribedText(entry.getTranscribedText());
            transcriptRepository.save(transcript);
        }

        return "Manual transcript saved for submission: " + submissionId;
    }
}
