package com.examplatform.modules.written.question.controller;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.question.service.GeminiAnswerGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/written-questions")
@RequiredArgsConstructor
public class WrittenQuestionAiAnswerController {

    private final WrittenQuestionRepository questionRepository;
    private final GeminiAnswerGeneratorService geminiService;

    /**
     * নির্দিষ্ট question-এর নির্দিষ্ট part-এর জন্য AI answer generate করে সেভ করে
     * part = A, B, C, D
     */
    @PostMapping("/{questionId}/generate-ai-answer/{part}")
    public ResponseEntity<?> generateAiAnswer(
            @PathVariable String questionId,
            @PathVariable String part) {

        WrittenQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question পাওয়া যায়নি: " + questionId));

        String stimulus = question.getStimulus();
        String questionText;
        int maxMark;
        String generatedAnswer;

        switch (part.toUpperCase()) {
            case "A" -> {
                questionText = question.getPartAQuestion();
                maxMark = question.getPartAMaxMark().intValue();
                generatedAnswer = geminiService.generateReferenceAnswer(stimulus, questionText, maxMark);
                question.setPartAAiAnswer(generatedAnswer);
            }
            case "B" -> {
                questionText = question.getPartBQuestion();
                maxMark = question.getPartBMaxMark().intValue();
                generatedAnswer = geminiService.generateReferenceAnswer(stimulus, questionText, maxMark);
                question.setPartBAiAnswer(generatedAnswer);
            }
            case "C" -> {
                questionText = question.getPartCQuestion();
                maxMark = question.getPartCMaxMark().intValue();
                generatedAnswer = geminiService.generateReferenceAnswer(stimulus, questionText, maxMark);
                question.setPartCAiAnswer(generatedAnswer);
            }
            case "D" -> {
                questionText = question.getPartDQuestion();
                maxMark = question.getPartDMaxMark().intValue();
                generatedAnswer = geminiService.generateReferenceAnswer(stimulus, questionText, maxMark);
                question.setPartDAiAnswer(generatedAnswer);
            }
            default -> throw new IllegalArgumentException("Invalid part: " + part + " (A/B/C/D হতে হবে)");
        }

        questionRepository.save(question);

        return ResponseEntity.ok(new AiAnswerResponse(questionId, part.toUpperCase(), generatedAnswer));
    }

    /**
     * Admin generated AI answer edit করে final করলে এই endpoint দিয়ে আপডেট হবে
     */
    @PutMapping("/{questionId}/ai-answer/{part}")
    public ResponseEntity<?> updateAiAnswer(
            @PathVariable String questionId,
            @PathVariable String part,
            @RequestBody UpdateAiAnswerRequest request) {

        WrittenQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question পাওয়া যায়নি: " + questionId));

        switch (part.toUpperCase()) {
            case "A" -> question.setPartAAiAnswer(request.aiAnswer());
            case "B" -> question.setPartBAiAnswer(request.aiAnswer());
            case "C" -> question.setPartCAiAnswer(request.aiAnswer());
            case "D" -> question.setPartDAiAnswer(request.aiAnswer());
            default -> throw new IllegalArgumentException("Invalid part: " + part);
        }

        questionRepository.save(question);
        return ResponseEntity.ok(new AiAnswerResponse(questionId, part.toUpperCase(), request.aiAnswer()));
    }

    public record AiAnswerResponse(String questionId, String part, String aiAnswer) {}
    public record UpdateAiAnswerRequest(String aiAnswer) {}
}