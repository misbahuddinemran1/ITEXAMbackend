package com.examplatform.modules.written.question.mapper;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.entity.WrittenQuestionPart;
import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.QuestionPartRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionPartAnswerResponse;
import com.examplatform.modules.written.question.response.QuestionPartResponse;
import com.examplatform.modules.written.question.response.QuestionPartStudentResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.response.QuestionWithAnswerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class WrittenQuestionMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public WrittenQuestion toEntity(CreateQuestionRequest req) {
        WrittenQuestion question = WrittenQuestion.builder()
                .examId(req.getExamId())
                .subject(findSubject(req.getSubjectId()))
                .chapter(findChapter(req.getChapterId()))
                .topic(findTopic(req.getTopicId()))
                .questionOrder(req.getQuestionOrder() != null ? req.getQuestionOrder() : 1)
                .stimulus(req.getStimulus())
                .stimulusBn(req.getStimulusBn())
                .isBoardQuestion(req.isBoardQuestion())
                .board(req.getBoard())
                .examYear(req.getExamYear())
                .build();

        question.setParts(buildParts(question, req.getParts()));
        return question;
    }

    public void applyUpdate(WrittenQuestion q, UpdateQuestionRequest req) {
        if (req.getSubjectId() != null) q.setSubject(findSubject(req.getSubjectId()));
        if (req.getChapterId() != null) q.setChapter(findChapter(req.getChapterId()));
        if (req.getTopicId() != null) {
            q.setTopic(req.getTopicId().isBlank() ? null : findTopic(req.getTopicId()));
        }

        if (req.getQuestionOrder() != null) q.setQuestionOrder(req.getQuestionOrder());
        if (req.getStimulus() != null) q.setStimulus(req.getStimulus());
        if (req.getStimulusBn() != null) q.setStimulusBn(req.getStimulusBn());

        if (req.getIsBoardQuestion() != null) q.setBoardQuestion(req.getIsBoardQuestion());
        if (req.getBoard() != null) q.setBoard(req.getBoard());
        if (req.getExamYear() != null) q.setExamYear(req.getExamYear());

        // parts পাঠানো হলে পুরনো সব part বাদ দিয়ে নতুনগুলো বসবে (orphanRemoval থাকায় পুরনোগুলো ডিলিট হয়ে যাবে)
        if (req.getParts() != null) {
            q.getParts().clear();
            q.getParts().addAll(buildParts(q, req.getParts()));
        }
    }

    private List<WrittenQuestionPart> buildParts(WrittenQuestion question, List<QuestionPartRequest> partRequests) {
        List<WrittenQuestionPart> parts = new ArrayList<>();
        if (partRequests == null) return parts;

        int order = 1;
        for (QuestionPartRequest pr : partRequests) {
            parts.add(WrittenQuestionPart.builder()
                    .question(question)
                    .partOrder(order++)
                    .questionText(pr.getQuestionText())
                    .modelAnswer(pr.getModelAnswer())
                    .markingScheme(pr.getMarkingScheme())
                    .maxMark(pr.getMaxMark())
                    .build());
        }
        return parts;
    }

    public QuestionAdminResponse toAdminResponse(WrittenQuestion q) {
        return QuestionAdminResponse.builder()
                .id(q.getId())
                .examId(q.getExamId())
                .subjectId(q.getSubject().getId())
                .subjectName(q.getSubject().getName())
                .chapterId(q.getChapter().getId())
                .chapterName(q.getChapter().getName())
                .topicId(q.getTopic() != null ? q.getTopic().getId() : null)
                .topicName(q.getTopic() != null ? q.getTopic().getName() : null)
                .questionOrder(q.getQuestionOrder())
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .isBoardQuestion(q.isBoardQuestion())
                .board(q.getBoard())
                .examYear(q.getExamYear())
                .parts(toPartResponses(q))
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    public QuestionStudentResponse toStudentResponse(WrittenQuestion q) {
        return QuestionStudentResponse.builder()
                .id(q.getId())
                .questionOrder(q.getQuestionOrder())
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .parts(toPartStudentResponses(q))
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    public QuestionWithAnswerResponse toWithAnswerResponse(WrittenQuestion q) {
        return QuestionWithAnswerResponse.builder()
                .id(q.getId())
                .questionOrder(q.getQuestionOrder())
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .parts(toPartAnswerResponses(q))
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    // ---- part-list conversion, নতুন `parts` থাকলে সেটা, না থাকলে (পুরনো data) legacy A/B/C/D fallback ----

    private List<QuestionPartResponse> toPartResponses(WrittenQuestion q) {
        if (q.getParts() != null && !q.getParts().isEmpty()) {
            return q.getParts().stream()
                    .map(p -> QuestionPartResponse.builder()
                            .id(p.getId())
                            .partOrder(p.getPartOrder())
                            .questionText(p.getQuestionText())
                            .modelAnswer(p.getModelAnswer())
                            .aiAnswer(p.getAiAnswer())
                            .markingScheme(p.getMarkingScheme())
                            .maxMark(p.getMaxMark())
                            .build())
                    .toList();
        }
        return legacyPartResponses(q);
    }

    private List<QuestionPartStudentResponse> toPartStudentResponses(WrittenQuestion q) {
        if (q.getParts() != null && !q.getParts().isEmpty()) {
            return q.getParts().stream()
                    .map(p -> QuestionPartStudentResponse.builder()
                            .partOrder(p.getPartOrder())
                            .questionText(p.getQuestionText())
                            .maxMark(p.getMaxMark())
                            .build())
                    .toList();
        }
        return legacyPartResponses(q).stream()
                .map(p -> QuestionPartStudentResponse.builder()
                        .partOrder(p.getPartOrder())
                        .questionText(p.getQuestionText())
                        .maxMark(p.getMaxMark())
                        .build())
                .toList();
    }

    private List<QuestionPartAnswerResponse> toPartAnswerResponses(WrittenQuestion q) {
        if (q.getParts() != null && !q.getParts().isEmpty()) {
            return q.getParts().stream()
                    .map(p -> QuestionPartAnswerResponse.builder()
                            .partOrder(p.getPartOrder())
                            .questionText(p.getQuestionText())
                            .answer(resolveAnswer(p.getModelAnswer(), p.getAiAnswer()))
                            .isAi(isAiAnswer(p.getModelAnswer(), p.getAiAnswer()))
                            .maxMark(p.getMaxMark())
                            .build())
                    .toList();
        }
        return legacyPartResponses(q).stream()
                .map(p -> QuestionPartAnswerResponse.builder()
                        .partOrder(p.getPartOrder())
                        .questionText(p.getQuestionText())
                        .answer(resolveAnswer(p.getModelAnswer(), p.getAiAnswer()))
                        .isAi(isAiAnswer(p.getModelAnswer(), p.getAiAnswer()))
                        .maxMark(p.getMaxMark())
                        .build())
                .toList();
    }

    /** পুরনো (Phase-1 আগের) প্রশ্নগুলোর জন্য fixed A/B/C/D কলাম থেকে part লিস্ট বানায় (backward compatibility) */
    private List<QuestionPartResponse> legacyPartResponses(WrittenQuestion q) {
        List<QuestionPartResponse> legacy = new ArrayList<>();
        addLegacyPart(legacy, 1, q.getPartAQuestion(), q.getPartAModelAnswer(), q.getPartAAiAnswer(), q.getPartAMarkingScheme(), q.getPartAMaxMark());
        addLegacyPart(legacy, 2, q.getPartBQuestion(), q.getPartBModelAnswer(), q.getPartBAiAnswer(), q.getPartBMarkingScheme(), q.getPartBMaxMark());
        addLegacyPart(legacy, 3, q.getPartCQuestion(), q.getPartCModelAnswer(), q.getPartCAiAnswer(), q.getPartCMarkingScheme(), q.getPartCMaxMark());
        addLegacyPart(legacy, 4, q.getPartDQuestion(), q.getPartDModelAnswer(), q.getPartDAiAnswer(), q.getPartDMarkingScheme(), q.getPartDMaxMark());
        return legacy;
    }

    private void addLegacyPart(List<QuestionPartResponse> list, int order, String questionText, String modelAnswer,
                                String aiAnswer, String markingScheme, java.math.BigDecimal maxMark) {
        if (questionText == null || questionText.isBlank()) return;
        list.add(QuestionPartResponse.builder()
                .partOrder(order)
                .questionText(questionText)
                .modelAnswer(modelAnswer)
                .aiAnswer(aiAnswer)
                .markingScheme(markingScheme)
                .maxMark(maxMark)
                .build());
    }

    private String resolveAnswer(String modelAnswer, String aiAnswer) {
        if (modelAnswer != null && !modelAnswer.isBlank()) return modelAnswer;
        return aiAnswer;
    }

    private Boolean isAiAnswer(String modelAnswer, String aiAnswer) {
        if (modelAnswer != null && !modelAnswer.isBlank()) return false;
        return aiAnswer != null && !aiAnswer.isBlank();
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
        if (id == null || id.isBlank()) {
            return null;
        }
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + id));
    }
}
