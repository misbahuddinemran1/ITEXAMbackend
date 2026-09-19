package com.examplatform.modules.written.evaluation.ai.service;

import com.examplatform.modules.written.evaluation.ai.parser.TranscriptionResponseParser;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
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

    @Transactional
    public void ensureTranscribed(String examId, String submissionId,
                                   Map<String, List<Integer>> partsToTranscribeByQuestionId) {

        List<WrittenSubmissionFile> files = submissionFileRepository
                .findBySubmissionIdOrderByPageNumberAsc(submissionId);

        if (files.isEmpty()) {
            throw new IllegalStateException("No submission files found for submission: " + submissionId);
        }

        List<WrittenSubmissionTranscript> existing = transcriptRepository.findBySubmissionId(submissionId);
        Map<String, WrittenSubmissionTranscript> existingByKey = new HashMap<>();
        for (WrittenSubmissionTranscript t : existing) {
            existingByKey.put(t.getQuestion().getId() + ":" + t.getPartOrder(), t);
        }

        Map<String, List<Integer>> missing = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : partsToTranscribeByQuestionId.entrySet()) {
            for (Integer partOrder : entry.getValue()) {
                String key = entry.getKey() + ":" + partOrder;
                if (!existingByKey.containsKey(key)) {
                    missing.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(partOrder);
                }
            }
        }

        if (missing.isEmpty()) {
            return;
        }

        FileType submissionFileType = files.get(0).getFileType();

        if (submissionFileType == FileType.TEXT) {
            transcribeFromTextContent(submissionId, files, missing);
        } else {
            transcribeFromImagesOrPdf(submissionId, files, missing, submissionFileType);
        }
    }

    private void transcribeFromTextContent(String submissionId, List<WrittenSubmissionFile> files,
                                            Map<String, List<Integer>> missing) {
        String combinedText = files.stream()
                .map(WrittenSubmissionFile::getTextContent)
                .filter(t -> t != null && !t.isBlank())
                .reduce("", (a, b) -> a + "\n" + b);

        for (Map.Entry<String, List<Integer>> entry : missing.entrySet()) {
            WrittenQuestion question = questionRepository.findById(entry.getKey())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Question not found: " + entry.getKey()));

            for (Integer partOrder : entry.getValue()) {
                saveTranscript(submissionId, question, partOrder, combinedText);
            }
        }
    }

    private void transcribeFromImagesOrPdf(String submissionId, List<WrittenSubmissionFile> files,
                                            Map<String, List<Integer>> missing, FileType fileType) {

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
            if (question == null) continue;

            int partOrder;
            try {
                partOrder = Integer.parseInt(entry.part().trim());
            } catch (NumberFormatException e) {
                continue;
            }
            saveTranscript(submissionId, question, partOrder, entry.transcribedText());
        }
    }

    private void saveTranscript(String submissionId, WrittenQuestion question, int partOrder, String text) {
        WrittenSubmissionTranscript transcript = WrittenSubmissionTranscript.builder()
                .submissionId(submissionId)
                .question(question)
                .partOrder(partOrder)
                .transcribedText(text)
                .build();
        transcriptRepository.save(transcript);
    }
}
