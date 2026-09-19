package com.examplatform.modules.written.question.controller;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.entity.WrittenQuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionPartRepository;
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
    private final WrittenQuestionPartRepository partRepository;
    private final GeminiAnswerGeneratorService geminiService;

    /**
     * নির্দিষ্ট question-এর নির্দিষ্ট part-এর জন্য AI answer generate করে সেভ করে।
     * partOrder = 1, 2, 3... (আর ফিক্সড A/B/C/D না — admin যত part দিয়েছে তার order)
     */
    @PostMapping("/{questionId}/generate-ai-answer/{partOrder}")
    public ResponseEntity<?> generateAiAnswer(
            @PathVariable String questionId,
            @PathVariable Integer partOrder) {

        WrittenQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question পাওয়া যায়নি: " + questionId));

        WrittenQuestionPart part = partRepository.findByQuestionIdAndPartOrder(questionId, partOrder)
                .orElseThrow(() -> new RuntimeException("Part পাওয়া যায়নি: " + partOrder));

        String generatedAnswer = geminiService.generateReferenceAnswer(
                question.getStimulus(), part.getQuestionText(), part.getMaxMark().intValue());
        part.setAiAnswer(generatedAnswer);
        partRepository.save(part);

        return ResponseEntity.ok(new AiAnswerResponse(questionId, partOrder, generatedAnswer));
    }

    /**
     * Admin generated AI answer edit করে final করলে এই endpoint দিয়ে আপডেট হবে
     */
    @PutMapping("/{questionId}/ai-answer/{partOrder}")
    public ResponseEntity<?> updateAiAnswer(
            @PathVariable String questionId,
            @PathVariable Integer partOrder,
            @RequestBody UpdateAiAnswerRequest request) {

        WrittenQuestionPart part = partRepository.findByQuestionIdAndPartOrder(questionId, partOrder)
                .orElseThrow(() -> new RuntimeException("Part পাওয়া যায়নি: " + partOrder));

        part.setAiAnswer(request.aiAnswer());
        partRepository.save(part);

        return ResponseEntity.ok(new AiAnswerResponse(questionId, partOrder, request.aiAnswer()));
    }

    public record AiAnswerResponse(String questionId, Integer partOrder, String aiAnswer) {}
    public record UpdateAiAnswerRequest(String aiAnswer) {}
}
