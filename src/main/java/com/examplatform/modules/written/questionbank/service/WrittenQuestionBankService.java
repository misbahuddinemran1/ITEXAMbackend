package com.examplatform.modules.written.questionbank.service;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.question.service.GeminiAnswerGeneratorService;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBank;
import com.examplatform.modules.written.questionbank.mapper.WrittenQuestionBankMapper;
import com.examplatform.modules.written.questionbank.repository.WrittenQuestionBankRepository;
import com.examplatform.modules.written.questionbank.request.AttachToExamRequest;
import com.examplatform.modules.written.questionbank.request.CreateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.request.UpdateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.response.BankQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenQuestionBankService {

    private final WrittenQuestionBankRepository bankRepository;
    private final WrittenQuestionBankMapper bankMapper;
    private final GeminiAnswerGeneratorService geminiService;
    private final WrittenQuestionRepository writtenQuestionRepository;
    private final WrittenExamRepository writtenExamRepository;

    @Transactional
    public BankQuestionResponse createBankQuestion(CreateBankQuestionRequest req) {
        WrittenQuestionBank bank = bankMapper.toEntity(req);

        if (req.isAutoGenerateAiAnswer()) {
            generateAllAiAnswers(bank);
        }

        WrittenQuestionBank saved = bankRepository.save(bank);
        return bankMapper.toResponse(saved);
    }

    @Transactional
    public BankQuestionResponse updateBankQuestion(String id, UpdateBankQuestionRequest req) {
        WrittenQuestionBank bank = getBankOrThrow(id);
        bankMapper.applyUpdate(bank, req);

        // regenerateAiAnswer=true দিলে Gemini আবার কল হয়ে AI answer ওভাররাইট করবে;
        // নাহলে request এ সরাসরি দেওয়া partXAiAnswer (যদি থাকে) সেটাই থাকবে (manual override)
        if (req.isRegenerateAiAnswer()) {
            generateAllAiAnswers(bank);
        }

        WrittenQuestionBank saved = bankRepository.save(bank);
        return bankMapper.toResponse(saved);
    }

    private void generateAllAiAnswers(WrittenQuestionBank q) {
        try {
            if (notBlank(q.getPartAQuestion()) && q.getPartAMaxMark() != null) {
                q.setPartAAiAnswer(geminiService.generateReferenceAnswer(
                        q.getStimulus(), q.getPartAQuestion(), q.getPartAMaxMark().intValue()));
            }
            if (notBlank(q.getPartBQuestion()) && q.getPartBMaxMark() != null) {
                q.setPartBAiAnswer(geminiService.generateReferenceAnswer(
                        q.getStimulus(), q.getPartBQuestion(), q.getPartBMaxMark().intValue()));
            }
            if (notBlank(q.getPartCQuestion()) && q.getPartCMaxMark() != null) {
                q.setPartCAiAnswer(geminiService.generateReferenceAnswer(
                        q.getStimulus(), q.getPartCQuestion(), q.getPartCMaxMark().intValue()));
            }
            if (notBlank(q.getPartDQuestion()) && q.getPartDMaxMark() != null) {
                q.setPartDAiAnswer(geminiService.generateReferenceAnswer(
                        q.getStimulus(), q.getPartDQuestion(), q.getPartDMaxMark().intValue()));
            }
        } catch (Exception e) {
            // AI ফেইল করলেও bank question সেভ হোক, পরে admin manually generate/edit করতে পারবে
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    public List<BankQuestionResponse> getAll() {
        return bankRepository.findAll().stream().map(bankMapper::toResponse).toList();
    }

    public List<BankQuestionResponse> getBySubject(String subjectId) {
        return bankRepository.findBySubjectId(subjectId).stream().map(bankMapper::toResponse).toList();
    }

    public List<BankQuestionResponse> getBySubjectAndChapter(String subjectId, String chapterId) {
        return bankRepository.findBySubjectIdAndChapterId(subjectId, chapterId).stream()
                .map(bankMapper::toResponse).toList();
    }

    public BankQuestionResponse getById(String id) {
        return bankMapper.toResponse(getBankOrThrow(id));
    }

    @Transactional
    public void deleteBankQuestion(String id) {
        bankRepository.delete(getBankOrThrow(id));
    }

    /**
     * Bank থেকে একটা বা একাধিক প্রশ্ন exam এ attach করে (copy করে WrittenQuestion বানিয়ে)।
     * Bank এর row টা bank এই থেকে যায় — বারবার reuse করা যাবে।
     */
    @Transactional
    public List<String> attachToExam(AttachToExamRequest req) {
        WrittenExam exam = writtenExamRepository.findById(req.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + req.getExamId()));

        int startOrder = writtenQuestionRepository.findByExamIdOrderByQuestionOrderAsc(exam.getId()).size() + 1;

        List<String> createdIds = new ArrayList<>();
        int order = startOrder;
        for (String bankId : req.getBankQuestionIds()) {
            WrittenQuestionBank bank = getBankOrThrow(bankId);
            WrittenQuestion newQuestion = bankMapper.toWrittenQuestion(bank, exam.getId(), order++);
            WrittenQuestion saved = writtenQuestionRepository.save(newQuestion);
            createdIds.add(saved.getId());
        }

        List<WrittenQuestion> all = writtenQuestionRepository.findByExamIdOrderByQuestionOrderAsc(exam.getId());
        BigDecimal total = all.stream().map(WrittenQuestion::getTotalMaxMark).reduce(BigDecimal.ZERO, BigDecimal::add);
        exam.setTotalMarks(total.intValue());
        writtenExamRepository.save(exam);

        return createdIds;
    }

    /**
     * একটা নির্দিষ্ট Part এর জন্য AI দিয়ে answer generate করে —
     * কিন্তু সেভ করে না, শুধু preview হিসেবে ফেরত দেয়।
     */
    public String generatePartAnswer(String id, String part) {
        WrittenQuestionBank q = getBankOrThrow(id);
        String questionText;
        BigDecimal maxMark;
        switch (part) {
            case "A" -> {
                questionText = q.getPartAQuestion();
                maxMark = q.getPartAMaxMark();
            }
            case "B" -> {
                questionText = q.getPartBQuestion();
                maxMark = q.getPartBMaxMark();
            }
            case "C" -> {
                questionText = q.getPartCQuestion();
                maxMark = q.getPartCMaxMark();
            }
            case "D" -> {
                questionText = q.getPartDQuestion();
                maxMark = q.getPartDMaxMark();
            }
            default -> throw new IllegalArgumentException("Invalid part: " + part);
        }
        if (questionText == null || questionText.isBlank() || maxMark == null) {
            throw new IllegalArgumentException("এই Part এর প্রশ্ন বা মার্ক সেট করা নেই, আগে Save করুন");
        }
        return geminiService.generateReferenceAnswer(q.getStimulus(), questionText, maxMark.intValue());
    }

    /**
     * Admin প্রিভিউ দেখে পছন্দ করলে এই answer টা নির্দিষ্ট Part এ সেভ করে।
     */
    @Transactional
    public BankQuestionResponse saveAiAnswer(String id, String part, String aiAnswer) {
        WrittenQuestionBank q = getBankOrThrow(id);
        switch (part) {
            case "A" -> q.setPartAAiAnswer(aiAnswer);
            case "B" -> q.setPartBAiAnswer(aiAnswer);
            case "C" -> q.setPartCAiAnswer(aiAnswer);
            case "D" -> q.setPartDAiAnswer(aiAnswer);
            default -> throw new IllegalArgumentException("Invalid part: " + part);
        }
        WrittenQuestionBank saved = bankRepository.save(q);
        return bankMapper.toResponse(saved);
    }

    private WrittenQuestionBank getBankOrThrow(String id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bank question not found: " + id));
    }
}
