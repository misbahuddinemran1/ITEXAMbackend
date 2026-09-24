package com.examplatform.modules.question.service;

import com.examplatform.common.dto.PageResponse;
import com.examplatform.common.exception.DuplicateResourceException;
import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.common.util.HashUtil;
import com.examplatform.modules.examtype.entity.ExamType;
import com.examplatform.modules.examtype.repository.ExamTypeRepository;
import com.examplatform.modules.question.dto.request.QuestionCreateRequest;
import com.examplatform.modules.question.dto.response.*;
import com.examplatform.modules.question.entity.*;
import com.examplatform.modules.question.repository.*;
import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;
import java.util.stream.Collectors;
import com.examplatform.modules.question.dto.request.OptionRequest;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;
    private final QuestionConceptRepository questionConceptRepository;
    private final QuestionTagRepository questionTagRepository;
    private final QuestionExamTypeRepository questionExamTypeRepository;
    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;
    private final ConceptRepository conceptRepository;
    private final TagRepository tagRepository;
    private final ExamTypeRepository examTypeRepository;

    @Transactional
    public QuestionResponse createQuestion(
            QuestionCreateRequest request) {

        // 1. Validate hierarchy
        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Subject", request.getSubjectId()));

        Chapter chapter = chapterRepository
                .findById(request.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Chapter", request.getChapterId()));

        Topic topic = topicRepository
                .findById(request.getTopicId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Topic", request.getTopicId()));

        // 2. Duplicate check
        String hash = HashUtil.sha256(
            HashUtil.normalizeText(request.getQuestionText())
        );
        if (questionRepository.existsByContentHash(hash)) {
            throw new DuplicateResourceException(
                "Duplicate question detected!"
            );
        }

        // 3. Parse enums
        Question.QuestionType qType;
        try {
            qType = Question.QuestionType.valueOf(
                request.getQuestionType().toUpperCase());
        } catch (Exception e) {
            qType = Question.QuestionType.MCQ_SINGLE;
        }

        Question.CognitiveLevel cogLevel;
        try {
            cogLevel = Question.CognitiveLevel.valueOf(
                request.getCognitiveLevel().toUpperCase());
        } catch (Exception e) {
            cogLevel = Question.CognitiveLevel.REMEMBER;
        }

        Question.Language lang;
        try {
            lang = Question.Language.valueOf(
                request.getLanguage().toUpperCase());
        } catch (Exception e) {
            lang = Question.Language.EN;
        }

        // 4. Save question
        Question question = Question.builder()
                .questionText(request.getQuestionText())
                .questionTextBn(request.getQuestionTextBn())
                .questionType(qType)
                .language(lang)
                .subject(subject)
                .chapter(chapter)
                .topic(topic)
                .difficultyLevel(request.getDifficultyLevel())
                .cognitiveLevel(cogLevel)
                .estimatedTimeSec(request.getEstimatedTimeSec())
                .sourceReference(request.getSourceReference())
                .yearAppeared(request.getYearAppeared())
                .contentHash(hash)
                .createdBy("system")
                .build();

        question = questionRepository.save(question);

        // 5. Save options
        List<Option> options = new ArrayList<>();
        if (request.getOptions() != null) {
            final Question savedQuestion = question;
            int[] index = {0};
            request.getOptions().forEach(opt -> {
                Option option = Option.builder()
                        .question(savedQuestion)
                        .optionKey(opt.getOptionKey())
                        .optionText(opt.getOptionText())
                        .optionTextBn(opt.getOptionTextBn())
                        .isCorrect(opt.isCorrect())
                        .explanation(opt.getExplanation())
                        .explanationBn(opt.getExplanationBn())
                        .orderIndex(index[0]++)
                        .build();
                options.add(optionRepository.save(option));
            });
        }

        // 6. Save concept mappings
        if (request.getConceptIds() != null) {
            final Question savedQuestion = question;
            boolean[] first = {true};
            request.getConceptIds().forEach(conceptId -> {
                conceptRepository.findById(conceptId)
                        .ifPresent(concept -> {
                            QuestionConcept qc = QuestionConcept
                                    .builder()
                                    .question(savedQuestion)
                                    .concept(concept)
                                    .weight(1.0)
                                    .isPrimary(first[0])
                                    .build();
                            questionConceptRepository.save(qc);
                            first[0] = false;
                        });
            });
        }

        // 7. Save tag mappings
        if (request.getTagIds() != null) {
            final Question savedQuestion = question;
            request.getTagIds().forEach(tagId -> {
                tagRepository.findById(tagId)
                        .ifPresent(tag -> {
                            QuestionTag qt = QuestionTag.builder()
                                    .id(new QuestionTag.QuestionTagId(
                                        savedQuestion.getId(),
                                        tag.getId()))
                                    .question(savedQuestion)
                                    .tag(tag)
                                    .build();
                            questionTagRepository.save(qt);
                        });
            });
        }

        // 8. Save exam type mappings
        if (request.getExamTypeIds() != null) {
            final Question savedQuestion = question;
            request.getExamTypeIds().forEach(examTypeId -> {
                examTypeRepository.findById(examTypeId)
                        .ifPresent(examType -> {
                            QuestionExamType qet = QuestionExamType
                                    .builder()
                                    .id(new QuestionExamType
                                        .QuestionExamTypeId(
                                            savedQuestion.getId(),
                                            examType.getId()))
                                    .question(savedQuestion)
                                    .examType(examType)
                                    .build();
                            questionExamTypeRepository.save(qet);
                        });
            });
        }

        return toResponse(question, options);
    }

    public PageResponse<QuestionResponse> getQuestions(
            String status,
            String subjectId,
            String chapterId,
            String topicId,
            Integer difficulty,
            Integer year,
            String source,
            String keyword,
            int page,
            int size){

        Question.QuestionStatus qStatus = null;
        if (status != null) {
            try {
                qStatus = Question.QuestionStatus.valueOf(
                    status.toUpperCase());
            } catch (Exception ignored) {}
        }

        var pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        var pageResult = questionRepository.findWithFilters(
                qStatus,
                subjectId,
                chapterId,
                topicId,
                difficulty,
                year,
                source == null ? "" : source.trim(),
                keyword == null ? "" : keyword.trim(),
                pageable
        );

        return PageResponse.of(pageResult.map(q -> {
            List<Option> opts = optionRepository
                    .findAllByQuestionIdOrderByOrderIndex(q.getId());
            return toResponse(q, opts);
        }));
    }

    public QuestionResponse getQuestion(String id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Question", id));
        List<Option> options = optionRepository
                .findAllByQuestionIdOrderByOrderIndex(id);
        return toResponse(question, options);
    }

    @Transactional
    public QuestionResponse updateStatus(String id, String status,
                                          String notes) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Question", id));

        Question.QuestionStatus newStatus =
                Question.QuestionStatus.valueOf(status.toUpperCase());
        question.setStatus(newStatus);
        if (notes != null) question.setReviewNotes(notes);

        questionRepository.save(question);

        List<Option> options = optionRepository
                .findAllByQuestionIdOrderByOrderIndex(id);
        return toResponse(question, options);
    }

    @Transactional
    public void deleteQuestion(String id) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                    new ResourceNotFoundException("Question", id));
        question.setStatus(Question.QuestionStatus.ARCHIVED);
        questionRepository.save(question);
    }

    @Transactional
    public QuestionResponse updateQuestion(
            String id,
            QuestionCreateRequest request) {

        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Question", id));

        Subject subject = subjectRepository
                .findById(request.getSubjectId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Subject",
                                request.getSubjectId()));

        Chapter chapter = chapterRepository
                .findById(request.getChapterId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chapter",
                                request.getChapterId()));

        Topic topic = topicRepository
                .findById(request.getTopicId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Topic",
                                request.getTopicId()));

        question.setQuestionText(request.getQuestionText());
        question.setQuestionTextBn(request.getQuestionTextBn());

        question.setSubject(subject);
        question.setChapter(chapter);
        question.setTopic(topic);

        question.setDifficultyLevel(
                request.getDifficultyLevel());

        question.setSourceReference(
                request.getSourceReference());

        question.setYearAppeared(
                request.getYearAppeared());

        questionRepository.save(question);

        // update old options
        List<Option> existingOptions =
                optionRepository
                        .findAllByQuestionIdOrderByOrderIndex(id);

        Map<String, Option> optionMap =
                existingOptions.stream()
                        .collect(Collectors.toMap(
                                Option::getOptionKey,
                                o -> o
                        ));

        List<Option> updatedOptions = new ArrayList<>();

        if (request.getOptions() != null) {

            int orderIndex = 0;

            for (OptionRequest reqOpt : request.getOptions()) {

                Option option =
                        optionMap.get(reqOpt.getOptionKey());

                if (option != null) {

                    option.setOptionText(
                            reqOpt.getOptionText());

                    option.setOptionTextBn(
                            reqOpt.getOptionTextBn());

                    option.setCorrect(
                            reqOpt.isCorrect());

                    option.setExplanation(
                            reqOpt.getExplanation());

                    option.setExplanationBn(
                            reqOpt.getExplanationBn());

                    option.setOrderIndex(orderIndex++);

                    updatedOptions.add(
                            optionRepository.save(option)
                    );

                } else {

                    Option newOption =
                            Option.builder()
                                    .question(question)
                                    .optionKey(reqOpt.getOptionKey())
                                    .optionText(reqOpt.getOptionText())
                                    .optionTextBn(reqOpt.getOptionTextBn())
                                    .isCorrect(reqOpt.isCorrect())
                                    .explanation(reqOpt.getExplanation())
                                    .explanationBn(reqOpt.getExplanationBn())
                                    .orderIndex(orderIndex++)
                                    .build();

                    updatedOptions.add(
                            optionRepository.save(newOption)
                    );
                }
            }
        }

        return toResponse(question, updatedOptions);


    }

  private QuestionResponse toResponse(Question q,
                                     List<Option> options) {
    List<OptionResponse> optionResponses = options.stream()
            .map(o -> OptionResponse.builder()
                    .id(o.getId())
                    .optionKey(o.getOptionKey())
                    .optionText(o.getOptionText())
                    .optionTextBn(o.getOptionTextBn())
                    .isCorrect(o.isCorrect())
                    .explanation(o.getExplanation())
                    .orderIndex(o.getOrderIndex())
                    .build())
            .toList();

    // Concepts
    List<ConceptResponse> conceptResponses =
            questionConceptRepository
                .findAllByQuestionId(q.getId())
                .stream()
                .map(qc -> ConceptResponse.builder()
                        .id(qc.getConcept().getId())
                        .name(qc.getConcept().getName())
                        .nameBn(qc.getConcept().getNameBn())
                        .conceptType(qc.getConcept()
                            .getConceptType().name())
                        .difficultyLevel(qc.getConcept()
                            .getDifficultyLevel())
                        .importanceScore(qc.getConcept()
                            .getImportanceScore())
                        .build())
                .toList();

    // Tags
    List<TagResponse> tagResponses =
            questionTagRepository
                .findAllByQuestionId(q.getId())
                .stream()
                .map(qt -> TagResponse.builder()
                        .id(qt.getTag().getId())
                        .name(qt.getTag().getName())
                        .tagType(qt.getTag().getTagType().name())
                        .colorCode(qt.getTag().getColorCode())
                        .build())
                .toList();

    // ExamTypes
    List<String> examTypeIds =
            questionExamTypeRepository
                .findAllByQuestionId(q.getId())
                .stream()
                .map(qet -> qet.getExamType().getId())
                .toList();

    return QuestionResponse.builder()
            .id(q.getId())
            .questionText(q.getQuestionText())
            .questionTextBn(q.getQuestionTextBn())
            .questionType(q.getQuestionType().name())
            .language(q.getLanguage().name())
            .subjectId(q.getSubject().getId())
            .subjectName(q.getSubject().getName())
            .chapterId(q.getChapter().getId())
            .chapterName(q.getChapter().getName())
            .topicId(q.getTopic().getId())
            .topicName(q.getTopic().getName())
            .difficultyLevel(q.getDifficultyLevel())
            .cognitiveLevel(q.getCognitiveLevel().name())
            .estimatedTimeSec(q.getEstimatedTimeSec())
            .sourceReference(q.getSourceReference())
            .yearAppeared(q.getYearAppeared())
            .status(q.getStatus().name())
            .options(optionResponses)
            .concepts(conceptResponses)
            .tags(tagResponses)
            .examTypeIds(examTypeIds)
            .createdAt(q.getCreatedAt().toString())
            .build();
}
}