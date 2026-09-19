package com.examplatform.modules.written.questionbank.mapper;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.entity.WrittenQuestionPart;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBank;
import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBankPart;
import com.examplatform.modules.written.questionbank.request.BankPartRequest;
import com.examplatform.modules.written.questionbank.request.CreateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.request.UpdateBankQuestionRequest;
import com.examplatform.modules.written.questionbank.response.BankPartResponse;
import com.examplatform.modules.written.questionbank.response.BankQuestionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class WrittenQuestionBankMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public WrittenQuestionBank toEntity(CreateBankQuestionRequest req) {
        WrittenQuestionBank bank = WrittenQuestionBank.builder()
                .subject(findSubject(req.getSubjectId()))
                .chapter(findChapter(req.getChapterId()))
                .topic(findTopic(req.getTopicId()))
                .stimulus(req.getStimulus())
                .stimulusBn(req.getStimulusBn())
                .isBoardQuestion(req.isBoardQuestion())
                .board(req.getBoard())
                .examYear(req.getExamYear())
                .build();

        bank.setParts(buildParts(bank, req.getParts(), null));
        return bank;
    }

    public void applyUpdate(WrittenQuestionBank q, UpdateBankQuestionRequest req) {
        if (req.getSubjectId() != null) q.setSubject(findSubject(req.getSubjectId()));
        if (req.getChapterId() != null) q.setChapter(findChapter(req.getChapterId()));
        if (req.getTopicId() != null) {
            q.setTopic(req.getTopicId().isBlank() ? null : findTopic(req.getTopicId()));
        }

        if (req.getStimulus() != null) q.setStimulus(req.getStimulus());
        if (req.getStimulusBn() != null) q.setStimulusBn(req.getStimulusBn());

        if (req.getIsBoardQuestion() != null) q.setBoardQuestion(req.getIsBoardQuestion());
        if (req.getBoard() != null) q.setBoard(req.getBoard());
        if (req.getExamYear() != null) q.setExamYear(req.getExamYear());

        if (req.getParts() != null) {
            Map<Integer, WrittenQuestionBankPart> oldByOrder = new HashMap<>();
            for (WrittenQuestionBankPart p : q.getParts()) {
                oldByOrder.put(p.getPartOrder(), p);
            }
            List<WrittenQuestionBankPart> rebuilt = buildParts(q, req.getParts(), oldByOrder);
            q.getParts().clear();
            q.getParts().addAll(rebuilt);
        }
    }

    private List<WrittenQuestionBankPart> buildParts(WrittenQuestionBank bank,
                                                     List<BankPartRequest> partRequests,
                                                     Map<Integer, WrittenQuestionBankPart> oldByOrder) {
        List<WrittenQuestionBankPart> parts = new ArrayList<>();
        if (partRequests == null) return parts;

        int order = 1;
        for (BankPartRequest pr : partRequests) {
            String aiAnswer = pr.getAiAnswer();
            if (aiAnswer == null && oldByOrder != null) {
                WrittenQuestionBankPart old = oldByOrder.get(order);
                if (old != null && java.util.Objects.equals(old.getQuestionText(), pr.getQuestionText())) {
                    aiAnswer = old.getAiAnswer();
                }
            }
            parts.add(WrittenQuestionBankPart.builder()
                    .bankQuestion(bank)
                    .partOrder(order++)
                    .questionText(pr.getQuestionText())
                    .modelAnswer(pr.getModelAnswer())
                    .aiAnswer(aiAnswer)
                    .markingScheme(pr.getMarkingScheme())
                    .maxMark(pr.getMaxMark())
                    .build());
        }
        return parts;
    }

    public BankQuestionResponse toResponse(WrittenQuestionBank q) {
        List<BankPartResponse> parts = q.getParts().stream()
                .map(p -> BankPartResponse.builder()
                        .id(p.getId())
                        .partOrder(p.getPartOrder())
                        .questionText(p.getQuestionText())
                        .modelAnswer(p.getModelAnswer())
                        .aiAnswer(p.getAiAnswer())
                        .markingScheme(p.getMarkingScheme())
                        .maxMark(p.getMaxMark())
                        .build())
                .toList();

        return BankQuestionResponse.builder()
                .id(q.getId())
                .subjectId(q.getSubject().getId())
                .subjectName(q.getSubject().getName())
                .chapterId(q.getChapter().getId())
                .chapterName(q.getChapter().getName())
                .topicId(q.getTopic() != null ? q.getTopic().getId() : null)
                .topicName(q.getTopic() != null ? q.getTopic().getName() : null)
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .isBoardQuestion(q.isBoardQuestion())
                .board(q.getBoard())
                .examYear(q.getExamYear())
                .parts(parts)
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    public WrittenQuestion toWrittenQuestion(WrittenQuestionBank bank, String examId, int questionOrder) {
        WrittenQuestion question = WrittenQuestion.builder()
                .examId(examId)
                .subject(bank.getSubject())
                .chapter(bank.getChapter())
                .topic(bank.getTopic())
                .questionOrder(questionOrder)
                .stimulus(bank.getStimulus())
                .stimulusBn(bank.getStimulusBn())
                .isBoardQuestion(bank.isBoardQuestion())
                .board(bank.getBoard())
                .examYear(bank.getExamYear())
                .build();

        List<WrittenQuestionPart> copied = new ArrayList<>();
        for (WrittenQuestionBankPart bp : bank.getParts()) {
            copied.add(WrittenQuestionPart.builder()
                    .question(question)
                    .partOrder(bp.getPartOrder())
                    .questionText(bp.getQuestionText())
                    .modelAnswer(bp.getModelAnswer())
                    .aiAnswer(bp.getAiAnswer())
                    .markingScheme(bp.getMarkingScheme())
                    .maxMark(bp.getMaxMark())
                    .build());
        }
        question.setParts(copied);
        return question;
    }

    private Subject findSubject(String id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Subject not found: " + id));
    }

    private Chapter findChapter(String id) {
        return chapterRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Chapter not found: " + id));
    }

    private Topic findTopic(String id) {
        if (id == null || id.isBlank()) return null;
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + id));
    }
}
