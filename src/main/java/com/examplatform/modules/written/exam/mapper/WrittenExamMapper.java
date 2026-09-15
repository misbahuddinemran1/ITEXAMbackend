package com.examplatform.modules.written.exam.mapper;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.taxonomy.repository.ChapterRepository;
import com.examplatform.modules.taxonomy.repository.SubjectRepository;
import com.examplatform.modules.taxonomy.repository.TopicRepository;
import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.AiProvider;
import com.examplatform.modules.written.exam.enums.EvaluationMode;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.enums.PartEvaluationMode;
import com.examplatform.modules.written.exam.request.CreateExamRequest;
import com.examplatform.modules.written.exam.request.UpdateExamRequest;
import com.examplatform.modules.written.exam.response.ExamResponse;
import com.examplatform.modules.written.exam.response.ExamSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class WrittenExamMapper {

    private final SubjectRepository subjectRepository;
    private final ChapterRepository chapterRepository;
    private final TopicRepository topicRepository;

    public WrittenExam toEntity(CreateExamRequest req) {
        EvaluationMode evaluationMode = EvaluationMode.valueOf(req.getEvaluationMode());

        WrittenExam.WrittenExamBuilder builder = WrittenExam.builder()
                .title(req.getTitle())
                .titleBn(req.getTitleBn())
                .description(req.getDescription())
                .educationLevel(req.getEducationLevel())
                .durationMinutes(req.getDurationMinutes())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .evaluationMode(evaluationMode)
                .aiProvider(resolveAiProvider(evaluationMode, req.getAiProvider()))
                .status(ExamStatus.DRAFT)
                .cycleNumber(1)
                .totalMarks(0)
                .practiceEnabled(req.getPracticeEnabled() != null ? req.getPracticeEnabled() : true)
                .showResultInPractice(req.getShowResultInPractice() != null ? req.getShowResultInPractice() : true);

        applyPartModes(builder, evaluationMode, req.getPartAMode(), req.getPartBMode(),
                req.getPartCMode(), req.getPartDMode());

        if (req.getSubjectId() != null) {
            builder.subject(findSubject(req.getSubjectId()));
        }
        if (req.getChapterId() != null) {
            builder.chapter(findChapter(req.getChapterId()));
        }
        if (req.getTopicId() != null) {
            builder.topic(findTopic(req.getTopicId()));
        }

        return builder.build();
    }

    public void applyUpdate(WrittenExam exam, UpdateExamRequest req) {
        if (req.getTitle() != null) exam.setTitle(req.getTitle());
        if (req.getTitleBn() != null) exam.setTitleBn(req.getTitleBn());
        if (req.getDescription() != null) exam.setDescription(req.getDescription());
        if (req.getEducationLevel() != null) exam.setEducationLevel(req.getEducationLevel());
        if (req.getDurationMinutes() != null) exam.setDurationMinutes(req.getDurationMinutes());
        if (req.getStartTime() != null) exam.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) exam.setEndTime(req.getEndTime());

        EvaluationMode evaluationMode = exam.getEvaluationMode();
        if (req.getEvaluationMode() != null) {
            evaluationMode = EvaluationMode.valueOf(req.getEvaluationMode());
            exam.setEvaluationMode(evaluationMode);
        }

        if (evaluationMode == EvaluationMode.AI || evaluationMode == EvaluationMode.HYBRID) {
            String providerStr = req.getAiProvider();
            AiProvider provider = providerStr != null ? AiProvider.valueOf(providerStr) : exam.getAiProvider();
            if (provider == null) {
                throw new IllegalArgumentException("aiProvider is required when evaluationMode is AI or HYBRID");
            }
            exam.setAiProvider(provider);
        } else if (req.getEvaluationMode() != null) {
            // switched to MANUAL — clear provider
            exam.setAiProvider(null);
        }

        if (evaluationMode == EvaluationMode.HYBRID) {
            if (req.getPartAMode() != null) exam.setPartAMode(PartEvaluationMode.valueOf(req.getPartAMode()));
            if (req.getPartBMode() != null) exam.setPartBMode(PartEvaluationMode.valueOf(req.getPartBMode()));
            if (req.getPartCMode() != null) exam.setPartCMode(PartEvaluationMode.valueOf(req.getPartCMode()));
            if (req.getPartDMode() != null) exam.setPartDMode(PartEvaluationMode.valueOf(req.getPartDMode()));
        } else if (req.getEvaluationMode() != null) {
            // MANUAL or AI applies uniformly to all parts
            PartEvaluationMode uniform = evaluationMode == EvaluationMode.AI
                    ? PartEvaluationMode.AI : PartEvaluationMode.MANUAL;
            exam.setPartAMode(uniform);
            exam.setPartBMode(uniform);
            exam.setPartCMode(uniform);
            exam.setPartDMode(uniform);
        }

        if (req.getPracticeEnabled() != null) exam.setPracticeEnabled(req.getPracticeEnabled());
        if (req.getShowResultInPractice() != null) exam.setShowResultInPractice(req.getShowResultInPractice());

        if (req.getSubjectId() != null) exam.setSubject(findSubject(req.getSubjectId()));
        if (req.getChapterId() != null) exam.setChapter(findChapter(req.getChapterId()));
        if (req.getTopicId() != null) exam.setTopic(findTopic(req.getTopicId()));
    }

    public ExamResponse toResponse(WrittenExam exam) {
        return ExamResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .titleBn(exam.getTitleBn())
                .description(exam.getDescription())
                .educationLevel(exam.getEducationLevel())
                .subjectId(exam.getSubject() != null ? exam.getSubject().getId() : null)
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .chapterId(exam.getChapter() != null ? exam.getChapter().getId() : null)
                .chapterName(exam.getChapter() != null ? exam.getChapter().getName() : null)
                .topicId(exam.getTopic() != null ? exam.getTopic().getId() : null)
                .topicName(exam.getTopic() != null ? exam.getTopic().getName() : null)
                .totalMarks(exam.getTotalMarks())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .cycleNumber(exam.getCycleNumber())
                .status(exam.getStatus().name())
                .evaluationMode(exam.getEvaluationMode().name())
                .aiProvider(exam.getAiProvider() != null ? exam.getAiProvider().name() : null)
                .partAMode(exam.getPartAMode() != null ? exam.getPartAMode().name() : null)
                .partBMode(exam.getPartBMode() != null ? exam.getPartBMode().name() : null)
                .partCMode(exam.getPartCMode() != null ? exam.getPartCMode().name() : null)
                .partDMode(exam.getPartDMode() != null ? exam.getPartDMode().name() : null)
                .createdAt(exam.getCreatedAt())
                .updatedAt(exam.getUpdatedAt())
                .practiceEnabled(exam.getPracticeEnabled())
                .showResultInPractice(exam.getShowResultInPractice())
                .build();
    }

    public ExamSummaryResponse toSummaryResponse(WrittenExam exam, boolean alreadyAttemptedThisCycle) {
        return ExamSummaryResponse.builder()
                .id(exam.getId())
                .title(exam.getTitle())
                .subjectName(exam.getSubject() != null ? exam.getSubject().getName() : null)
                .educationLevel(exam.getEducationLevel())
                .totalMarks(exam.getTotalMarks())
                .durationMinutes(exam.getDurationMinutes())
                .startTime(exam.getStartTime())
                .endTime(exam.getEndTime())
                .status(exam.getStatus().name())
                .alreadyAttemptedThisCycle(alreadyAttemptedThisCycle)
                .practiceEnabled(exam.getPracticeEnabled())
                .showResultInPractice(exam.getShowResultInPractice())
                .build();
    }

    private AiProvider resolveAiProvider(EvaluationMode evaluationMode, String aiProviderStr) {
        if (evaluationMode == EvaluationMode.AI || evaluationMode == EvaluationMode.HYBRID) {
            if (aiProviderStr == null) {
                throw new IllegalArgumentException("aiProvider is required when evaluationMode is AI or HYBRID");
            }
            return AiProvider.valueOf(aiProviderStr);
        }
        return null;
    }

    private void applyPartModes(WrittenExam.WrittenExamBuilder builder, EvaluationMode evaluationMode,
                                String partAStr, String partBStr, String partCStr, String partDStr) {
        if (evaluationMode == EvaluationMode.HYBRID) {
            builder.partAMode(partAStr != null ? PartEvaluationMode.valueOf(partAStr) : PartEvaluationMode.MANUAL);
            builder.partBMode(partBStr != null ? PartEvaluationMode.valueOf(partBStr) : PartEvaluationMode.MANUAL);
            builder.partCMode(partCStr != null ? PartEvaluationMode.valueOf(partCStr) : PartEvaluationMode.MANUAL);
            builder.partDMode(partDStr != null ? PartEvaluationMode.valueOf(partDStr) : PartEvaluationMode.MANUAL);
        } else {
            PartEvaluationMode uniform = evaluationMode == EvaluationMode.AI
                    ? PartEvaluationMode.AI : PartEvaluationMode.MANUAL;
            builder.partAMode(uniform);
            builder.partBMode(uniform);
            builder.partCMode(uniform);
            builder.partDMode(uniform);
        }
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
        return topicRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Topic not found: " + id));
    }
}
