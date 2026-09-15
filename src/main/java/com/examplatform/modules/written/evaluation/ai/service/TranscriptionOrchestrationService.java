package com.examplatform.modules.written.evaluation.ai.service;

import com.examplatform.modules.written.evaluation.ai.parser.TranscriptionResponseParser;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionFile;
import com.examplatform.modules.written.submission.entity.WrittenSubmissionTranscript;
import com.examplatform.modules.written.submission.enums.FileType;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionFileRepository;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionTranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TranscriptionOrchestrationService {

    private final WrittenSubmissionFileRepository submissionFileRepository;
    private final WrittenSubmissionTranscriptRepository transcriptRepository;
    private final WrittenQuestionRepository questionRepository;
    private final AiFileReaderService fileReaderService;
    private final GeminiTranscriptionService geminiTranscriptionService;

    /**
     * Ensures every (question, part) pair that needs AI evaluation has a transcript
     * cached for this submission. Skips already-transcribed pairs. Handles TEXT-type
     * submissions by copying text_content directly (no AI call needed for those).
     *
     * @param examId               the exam this submission belongs to
     * @param submissionId         the submission being evaluated
     * @param partsToTranscribeByQuestionId map of questionId -> list of parts that are AI-mode
     */
    @Transactional
    public void ensureTranscribed(String examId, String submissionId,
                                   Map<String, List<QuestionPart>> partsToTranscribeByQuestionId) {

        List<WrittenSubmissionFile> files = submissionFileRepository
                .findBySubmissionIdOrderByPageNumberAsc(submissionId);

        if (files.isEmpty()) {
            throw new IllegalStateException("No submission files found for submission: " + submissionId);
        }

        // Figure out which (questionId, part) pairs are already transcribed
        List<WrittenSubmissionTranscript> existing = transcriptRepository.findBySubmissionId(submissionId);
        Map<String, WrittenSubmissionTranscript> existingByKey = new HashMap<>();
        for (WrittenSubmissionTranscript t : existing) {
            existingByKey.put(t.getQuestion().getId() + ":" + t.getPart().name(), t);
        }

        Map<String, List<QuestionPart>> missing = new HashMap<>();
        for (Map.Entry<String, List<QuestionPart>> entry : partsToTranscribeByQuestionId.entrySet()) {
            for (QuestionPart part : entry.getValue()) {
                String key = entry.getKey() + ":" + part.name();
                if (!existingByKey.containsKey(key)) {
                    missing.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(part);
                }
            }
        }

        if (missing.isEmpty()) {
            return; // everything already transcribed/cached
        }

        FileType submissionFileType = files.get(0).getFileType();

        if (submissionFileType == FileType.TEXT) {
            transcribeFromTextContent(submissionId, files, missing);
        } else {
            transcribeFromImagesOrPdf(submissionId, files, missing, submissionFileType);
        }
    }

    private void transcribeFromTextContent(String submissionId, List<WrittenSubmissionFile> files,
                                            Map<String, List<QuestionPart>> missing) {
        // For TEXT submissions we assume a single combined text_content covering all parts.
        // Since there's no AI-based splitting here, this expects the student's text submission
        // to be organized in a way the matching step can still work on the whole blob per part.
        // Simplest approach: use the same full text_content as the "transcript" for every missing part.
        String combinedText = files.stream()
                .map(WrittenSubmissionFile::getTextContent)
                .filter(t -> t != null && !t.isBlank())
                .reduce("", (a, b) -> a + "\n" + b);

        for (Map.Entry<String, List<QuestionPart>> entry : missing.entrySet()) {
            WrittenQuestion question = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Question not found: " + entry.getKey()));

            for (QuestionPart part : entry.getValue()) {
                saveTranscript(submissionId, question, part, combinedText);
            }
        }
    }

    private void transcribeFromImagesOrPdf(String submissionId, List<WrittenSubmissionFile> files,
                                            Map<String, List<QuestionPart>> missing, FileType fileType) {

        List<WrittenQuestion> questions = missing.keySet().stream()
                .map(id -> questionRepository.findById(id)
                        .orElseThrow(() -> new java.util.NoSuchElementException("Question not found: " + id)))
                .toList();

        List<String> base64Images = files.stream()
                .map(f -> fileReaderService.readAsBase64(f.getFileUrl()))
                .toList();

        String mimeType = fileType == FileType.PDF ? "application/pdf" : fileReaderService.detectMimeType(files.get(0).getFileUrl());

        List<TranscriptionResponseParser.TranscriptEntry> entries =
                geminiTranscriptionService.transcribe(questions, missing, base64Images, mimeType);

        Map<String, WrittenQuestion> questionsById = new HashMap<>();
        for (WrittenQuestion q : questions) {
            questionsById.put(q.getId(), q);
        }

        for (TranscriptionResponseParser.TranscriptEntry entry : entries) {
            WrittenQuestion question = questionsById.get(entry.questionId());
            if (question == null) continue; // AI returned an unexpected questionId, skip defensively

            QuestionPart part = QuestionPart.valueOf(entry.part());
            saveTranscript(submissionId, question, part, entry.transcribedText());
        }
    }

    private void saveTranscript(String submissionId, WrittenQuestion question, QuestionPart part, String text) {
        WrittenSubmissionTranscript transcript = WrittenSubmissionTranscript.builder()
                .submissionId(submissionId)
                .question(question)
                .part(part)
                .transcribedText(text)
                .build();
        transcriptRepository.save(transcript);
    }
}
