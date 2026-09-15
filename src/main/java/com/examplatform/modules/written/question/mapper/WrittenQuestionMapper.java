package com.examplatform.modules.written.question.mapper;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class WrittenQuestionMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public WrittenQuestion toEntity(CreateQuestionRequest req) {
        return WrittenQuestion.builder()
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
                .partAQuestion(req.getPartAQuestion())
                .partAModelAnswer(req.getPartAModelAnswer())
                .partAMarkingScheme(req.getPartAMarkingScheme())
                .partAMaxMark(req.getPartAMaxMark())
                .partBQuestion(req.getPartBQuestion())
                .partBModelAnswer(req.getPartBModelAnswer())
                .partBMarkingScheme(req.getPartBMarkingScheme())
                .partBMaxMark(req.getPartBMaxMark())
                .partCQuestion(req.getPartCQuestion())
                .partCModelAnswer(req.getPartCModelAnswer())
                .partCMarkingScheme(req.getPartCMarkingScheme())
                .partCMaxMark(req.getPartCMaxMark())
                .partDQuestion(req.getPartDQuestion())
                .partDModelAnswer(req.getPartDModelAnswer())
                .partDMarkingScheme(req.getPartDMarkingScheme())
                .partDMaxMark(req.getPartDMaxMark())
                .build();
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

        if (req.getPartAQuestion() != null) q.setPartAQuestion(req.getPartAQuestion());
        if (req.getPartAModelAnswer() != null) q.setPartAModelAnswer(req.getPartAModelAnswer());
        if (req.getPartAMarkingScheme() != null) q.setPartAMarkingScheme(req.getPartAMarkingScheme());
        if (req.getPartAMaxMark() != null) q.setPartAMaxMark(req.getPartAMaxMark());

        if (req.getPartBQuestion() != null) q.setPartBQuestion(req.getPartBQuestion());
        if (req.getPartBModelAnswer() != null) q.setPartBModelAnswer(req.getPartBModelAnswer());
        if (req.getPartBMarkingScheme() != null) q.setPartBMarkingScheme(req.getPartBMarkingScheme());
        if (req.getPartBMaxMark() != null) q.setPartBMaxMark(req.getPartBMaxMark());

        if (req.getPartCQuestion() != null) q.setPartCQuestion(req.getPartCQuestion());
        if (req.getPartCModelAnswer() != null) q.setPartCModelAnswer(req.getPartCModelAnswer());
        if (req.getPartCMarkingScheme() != null) q.setPartCMarkingScheme(req.getPartCMarkingScheme());
        if (req.getPartCMaxMark() != null) q.setPartCMaxMark(req.getPartCMaxMark());

        if (req.getPartDQuestion() != null) q.setPartDQuestion(req.getPartDQuestion());
        if (req.getPartDModelAnswer() != null) q.setPartDModelAnswer(req.getPartDModelAnswer());
        if (req.getPartDMarkingScheme() != null) q.setPartDMarkingScheme(req.getPartDMarkingScheme());
        if (req.getPartDMaxMark() != null) q.setPartDMaxMark(req.getPartDMaxMark());
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
                .partAQuestion(q.getPartAQuestion())
                .partAModelAnswer(q.getPartAModelAnswer())
                .partAAiAnswer(q.getPartAAiAnswer())
                .partAMarkingScheme(q.getPartAMarkingScheme())
                .partAMaxMark(q.getPartAMaxMark())
                .partBQuestion(q.getPartBQuestion())
                .partBModelAnswer(q.getPartBModelAnswer())
                .partBAiAnswer(q.getPartBAiAnswer())
                .partBMarkingScheme(q.getPartBMarkingScheme())
                .partBMaxMark(q.getPartBMaxMark())
                .partCQuestion(q.getPartCQuestion())
                .partCModelAnswer(q.getPartCModelAnswer())
                .partCAiAnswer(q.getPartCAiAnswer())
                .partCMarkingScheme(q.getPartCMarkingScheme())
                .partCMaxMark(q.getPartCMaxMark())
                .partDQuestion(q.getPartDQuestion())
                .partDModelAnswer(q.getPartDModelAnswer())
                .partDAiAnswer(q.getPartDAiAnswer())
                .partDMarkingScheme(q.getPartDMarkingScheme())
                .partDMaxMark(q.getPartDMaxMark())
                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    public QuestionStudentResponse toStudentResponse(WrittenQuestion q) {
        return QuestionStudentResponse.builder()
                .id(q.getId())
                .questionOrder(q.getQuestionOrder())
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())
                .partAQuestion(q.getPartAQuestion())
                .partAMaxMark(q.getPartAMaxMark())
                .partBQuestion(q.getPartBQuestion())
                .partBMaxMark(q.getPartBMaxMark())
                .partCQuestion(q.getPartCQuestion())
                .partCMaxMark(q.getPartCMaxMark())
                .partDQuestion(q.getPartDQuestion())
                .partDMaxMark(q.getPartDMaxMark())
                .totalMaxMark(q.getTotalMaxMark())
                .build();
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
