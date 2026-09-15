package com.examplatform.modules.written.questionbank.controller;

import com.examplatform.modules.written.questionbank.request.AttachToExamRequest;
import com.examplatform.modules.written.questionbank.request.CreateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.response.BankQuestionResponse;
import com.examplatform.modules.written.questionbank.service.WrittenQuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.examplatform.modules.written.questionbank.request.UpdateBankQuestionRequest;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/written/question-bank")
@RequiredArgsConstructor
public class AdminWrittenQuestionBankController {

    private final WrittenQuestionBankService bankService;

    @PostMapping
    public BankQuestionResponse create(@RequestBody CreateBankQuestionRequest request) {
        return bankService.createBankQuestion(request);
    }

    @GetMapping
    public List<BankQuestionResponse> getAll() {
        return bankService.getAll();
    }

    @GetMapping("/subject/{subjectId}")
    public List<BankQuestionResponse> getBySubject(@PathVariable String subjectId) {
        return bankService.getBySubject(subjectId);
    }

    @GetMapping("/subject/{subjectId}/chapter/{chapterId}")
    public List<BankQuestionResponse> getBySubjectAndChapter(
            @PathVariable String subjectId, @PathVariable String chapterId) {
        return bankService.getBySubjectAndChapter(subjectId, chapterId);
    }

    @GetMapping("/{id}")
    public BankQuestionResponse getById(@PathVariable String id) {
        return bankService.getById(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        bankService.deleteBankQuestion(id);
    }

    /**
     * Bank থেকে একাধিক প্রশ্ন select করে exam এ attach করে
     */
    @PostMapping("/attach-to-exam")
    public List<String> attachToExam(@RequestBody AttachToExamRequest request) {
        return bankService.attachToExam(request);
    }

    @PutMapping("/{id}")
    public BankQuestionResponse update(@PathVariable String id, @RequestBody UpdateBankQuestionRequest request) {
        return bankService.updateBankQuestion(id, request);
    }

    /**
     * নির্দিষ্ট Part এর জন্য AI answer generate করে (preview only, সেভ হয় না)
     */
    @PostMapping("/{id}/generate-ai-answer/{part}")
    public Map<String, String> generateAiAnswer(@PathVariable String id, @PathVariable String part) {
        String answer = bankService.generatePartAnswer(id, part);
        return Map.of("aiAnswer", answer);
    }

    /**
     * Preview দেখে পছন্দ হলে নির্দিষ্ট Part এর AI answer সেভ করে
     */
    @PutMapping("/{id}/ai-answer/{part}")
    public BankQuestionResponse saveAiAnswer(@PathVariable String id, @PathVariable String part,
                                              @RequestBody Map<String, String> body) {
        return bankService.saveAiAnswer(id, part, body.get("aiAnswer"));
    }
}
