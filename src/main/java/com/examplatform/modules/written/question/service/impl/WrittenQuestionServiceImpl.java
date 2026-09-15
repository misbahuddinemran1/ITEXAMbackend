package com.examplatform.modules.written.question.service.impl;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.mapper.WrittenQuestionMapper;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.question.request.CreateQuestionRequest;
import com.examplatform.modules.written.question.request.ReorderQuestionsRequest;
import com.examplatform.modules.written.question.request.UpdateQuestionRequest;
import com.examplatform.modules.written.question.response.QuestionAdminResponse;
import com.examplatform.modules.written.question.response.QuestionStudentResponse;
import com.examplatform.modules.written.question.response.QuestionWithAnswerResponse;
import com.examplatform.modules.written.question.service.GeminiAnswerGeneratorService;
import com.examplatform.modules.written.question.service.WrittenQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class WrittenQuestionServiceImpl implements WrittenQuestionService {

    private static final Set<ExamStatus> FINISHED_ELIGIBLE_STATUSES = EnumSet.of(ExamStatus.LIVE, ExamStatus.ENDED, ExamStatus.ARCHIVED);

    private final WrittenQuestionRepository questionRepository;
    private final WrittenQuestionMapper questionMapper;
    private final WrittenExamRepository examRepository;
    private final GeminiAnswerGeneratorService geminiService;

    @Override
    @Transactional
    public QuestionAdminResponse createQuestion(CreateQuestionRequest request) {
        WrittenExam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + request.getExamId()));

        WrittenQuestion question = questionMapper.toEntity(request);

        // request.autoGenerateAiAnswer=true হলে manual answer + AI answer একসাথেই save হবে
        if (request.isAutoGenerateAiAnswer()) {
            generateAllAiAnswers(question);
        }

        WrittenQuestion saved = questionRepository.save(question);

        recalculateExamTotalMarks(exam.getId());

        return questionMapper.toAdminResponse(saved);
    }

    private void generateAllAiAnswers(WrittenQuestion q) {
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
            // AI generate ফেইল করলেও question save হোক, পরে আলাদা endpoint দিয়ে retry করা যাবে
        }
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    @Override
    @Transactional
    public QuestionAdminResponse updateQuestion(String questionId, UpdateQuestionRequest request) {
        WrittenQuestion question = getQuestionOrThrow(questionId);
        questionMapper.applyUpdate(question, request);
        WrittenQuestion updated = questionRepository.save(question);

        recalculateExamTotalMarks(question.getExamId());

        return questionMapper.toAdminResponse(updated);
    }

    @Override
    @Transactional
    public void reorderQuestions(String examId, ReorderQuestionsRequest request) {
        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        request.getItems().forEach(item -> {
            questions.stream()
                    .filter(q -> q.getId().equals(item.getQuestionId()))
                    .findFirst()
                    .ifPresent(q -> q.setQuestionOrder(item.getNewOrder()));
        });

        questionRepository.saveAll(questions);
    }

    @Override
    @Transactional
    public void deleteQuestion(String questionId) {
        WrittenQuestion question = getQuestionOrThrow(questionId);
        String examId = question.getExamId();
        questionRepository.delete(question);

        recalculateExamTotalMarks(examId);
    }

    @Override
    public List<QuestionAdminResponse> getQuestionsForAdmin(String examId) {
        return questionRepository.findByExamIdOrderByQuestionOrderAsc(examId).stream()
                .map(questionMapper::toAdminResponse)
                .toList();
    }

    @Override
    public List<QuestionStudentResponse> getQuestionsForStudent(String examId) {
        return questionRepository.findByExamIdOrderByQuestionOrderAsc(examId).stream()
                .map(questionMapper::toStudentResponse)
                .toList();
    }

    @Override
    public QuestionAdminResponse getQuestionByIdForAdmin(String questionId) {
        return questionMapper.toAdminResponse(getQuestionOrThrow(questionId));
    }

    @Override
    public List<QuestionWithAnswerResponse> getQuestionsWithAnswers(String examId) {
        WrittenExam exam = examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));

        boolean isFinished = FINISHED_ELIGIBLE_STATUSES.contains(exam.getStatus())
                && exam.getEndTime() != null
                && exam.getEndTime().isBefore(LocalDateTime.now());

        if (!isFinished) {
            throw new IllegalStateException("Exam is not finished yet: " + examId);
        }

        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        return questions.stream()
                .map(this::toQuestionWithAnswerResponse)
                .toList();
    }

    private QuestionWithAnswerResponse toQuestionWithAnswerResponse(WrittenQuestion q) {
        return QuestionWithAnswerResponse.builder()
                .id(q.getId())
                .questionOrder(q.getQuestionOrder())
                .stimulus(q.getStimulus())
                .stimulusBn(q.getStimulusBn())

                .partAQuestion(q.getPartAQuestion())
                .partAAnswer(resolveAnswer(q.getPartAModelAnswer(), q.getPartAAiAnswer()))
                .partAIsAi(isAiAnswer(q.getPartAModelAnswer(), q.getPartAAiAnswer()))
                .partAMaxMark(q.getPartAMaxMark())

                .partBQuestion(q.getPartBQuestion())
                .partBAnswer(resolveAnswer(q.getPartBModelAnswer(), q.getPartBAiAnswer()))
                .partBIsAi(isAiAnswer(q.getPartBModelAnswer(), q.getPartBAiAnswer()))
                .partBMaxMark(q.getPartBMaxMark())

                .partCQuestion(q.getPartCQuestion())
                .partCAnswer(resolveAnswer(q.getPartCModelAnswer(), q.getPartCAiAnswer()))
                .partCIsAi(isAiAnswer(q.getPartCModelAnswer(), q.getPartCAiAnswer()))
                .partCMaxMark(q.getPartCMaxMark())

                .partDQuestion(q.getPartDQuestion())
                .partDAnswer(resolveAnswer(q.getPartDModelAnswer(), q.getPartDAiAnswer()))
                .partDIsAi(isAiAnswer(q.getPartDModelAnswer(), q.getPartDAiAnswer()))
                .partDMaxMark(q.getPartDMaxMark())

                .totalMaxMark(q.getTotalMaxMark())
                .build();
    }

    private String resolveAnswer(String modelAnswer, String aiAnswer) {
        if (modelAnswer != null && !modelAnswer.isBlank()) {
            return modelAnswer;
        }
        return aiAnswer;
    }

    private Boolean isAiAnswer(String modelAnswer, String aiAnswer) {
        if (modelAnswer != null && !modelAnswer.isBlank()) {
            return false;
        }
        return aiAnswer != null && !aiAnswer.isBlank();
    }

    private WrittenQuestion getQuestionOrThrow(String questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new NoSuchElementException("Question not found: " + questionId));
    }

    /**
     * Keeps written_exam.total_marks in sync with the sum of all its questions' max marks.
     */
    private void recalculateExamTotalMarks(String examId) {
        List<WrittenQuestion> questions = questionRepository.findByExamIdOrderByQuestionOrderAsc(examId);

        BigDecimal total = questions.stream()
                .map(WrittenQuestion::getTotalMaxMark)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        examRepository.findById(examId).ifPresent(exam -> {
            exam.setTotalMarks(total.intValue());
            examRepository.save(exam);
        });
    }
}
