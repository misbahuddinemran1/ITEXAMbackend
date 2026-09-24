package com.examplatform.modules.written.questionbank.service;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.question.service.GeminiAnswerGeneratorService;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBank;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBankPart;
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
            for (WrittenQuestionBankPart part : q.getParts()) {
                if (notBlank(part.getQuestionText()) && part.getMaxMark() != null) {
                    part.setAiAnswer(geminiService.generateReferenceAnswer(
                            q.getStimulus(), part.getQuestionText(), part.getMaxMark().intValue()));
                }
            }
        } catch (Exception e) {
            // AI ফেইল করলেও bank question সেভ হোক
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

    public List<BankQuestionResponse> getByBoardFilter(String board, Integer examYear) {
        boolean hasBoard = board != null && !board.isBlank();
        boolean hasYear = examYear != null;
        List<WrittenQuestionBank> list;
        if (hasBoard && hasYear) {
            list = bankRepository.findByBoardAndExamYear(board, examYear);
        } else if (hasBoard) {
            list = bankRepository.findByBoard(board);
        } else if (hasYear) {
            list = bankRepository.findByExamYear(examYear);
        } else {
            list = bankRepository.findByIsBoardQuestionTrue();
        }
        return list.stream().map(bankMapper::toResponse).toList();
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

    public String generatePartAnswer(String id, int partOrder) {
        WrittenQuestionBank q = getBankOrThrow(id);
        WrittenQuestionBankPart part = findPart(q, partOrder);
        if (!notBlank(part.getQuestionText()) || part.getMaxMark() == null) {
            throw new IllegalArgumentException("এই Part এর প্রশ্ন বা মার্ক সেট করা নেই, আগে Save করুন");
        }
        return geminiService.generateReferenceAnswer(q.getStimulus(), part.getQuestionText(), part.getMaxMark().intValue());
    }

    @Transactional
    public BankQuestionResponse saveAiAnswer(String id, int partOrder, String aiAnswer) {
        WrittenQuestionBank q = getBankOrThrow(id);
        findPart(q, partOrder).setAiAnswer(aiAnswer);
        WrittenQuestionBank saved = bankRepository.save(q);
        return bankMapper.toResponse(saved);
    }

    private WrittenQuestionBankPart findPart(WrittenQuestionBank q, int partOrder) {
        return q.getParts().stream()
                .filter(p -> p.getPartOrder() == partOrder)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid part: " + partOrder));
    }

    private WrittenQuestionBank getBankOrThrow(String id) {
        return bankRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Bank question not found: " + id));
    }
}
