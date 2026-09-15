package com.examplatform.modules.written.evaluation.manual.service.impl;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.entity.WrittenEvaluationDetail;
import com.examplatform.modules.written.evaluation.enums.EvaluationStatus;
import com.examplatform.modules.written.evaluation.manual.request.ManualEvaluationRequest;
import com.examplatform.modules.written.evaluation.manual.request.PartMarkRequest;
import com.examplatform.modules.written.evaluation.manual.service.WrittenManualEvaluationService;
import com.examplatform.modules.written.evaluation.mapper.WrittenEvaluationMapper;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.exam.enums.EvaluationMode;
import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import com.examplatform.modules.written.question.repository.WrittenQuestionRepository;
import com.examplatform.modules.written.submission.entity.WrittenSubmission;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenManualEvaluationServiceImpl implements WrittenManualEvaluationService {

    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository detailRepository;
    private final WrittenEvaluationMapper evaluationMapper;
    private final WrittenSubmissionRepository submissionRepository;
    private final WrittenQuestionRepository questionRepository;

    @Override
    @Transactional
    public EvaluationResponse submitManualEvaluation(String submissionId, ManualEvaluationRequest request, String adminId) {
        WrittenSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Submission not found: " + submissionId));

        if (submission.getStatus() != SubmissionStatus.SUBMITTED
                && submission.getStatus() != SubmissionStatus.UNDER_REVIEW) {
            throw new IllegalStateException("Submission must be SUBMITTED or UNDER_REVIEW to evaluate");
        }

        boolean isNewEvaluation = evaluationRepository.findBySubmissionId(submissionId).isEmpty();

        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseGet(() -> WrittenEvaluation.builder()
                        .submission(submission)
                        .evaluationMode(EvaluationMode.MANUAL)
                        .status(EvaluationStatus.PENDING)
                        .build());

        WrittenEvaluation savedEvaluation = evaluationRepository.save(evaluation);

        if (!isNewEvaluation) {
            // Re-evaluation — wipe previous detail rows before inserting fresh ones.
            // flush() is required here: Hibernate's default flush order runs INSERTs
            // before DELETEs, so without an explicit flush the new rows (same
            // evaluation_id+question_id+part) would hit uk_written_eval_detail_part
            // while the old rows are still present, causing a unique constraint violation.
            detailRepository.deleteByEvaluationId(savedEvaluation.getId());
            detailRepository.flush();
        }

        BigDecimal totalMark = BigDecimal.ZERO;

        for (PartMarkRequest partMark : request.getPartMarks()) {
            WrittenQuestion question = questionRepository.findById(partMark.getQuestionId())
                    .orElseThrow(() -> new NoSuchElementException("Question not found: " + partMark.getQuestionId()));

            QuestionPart part = QuestionPart.valueOf(partMark.getPart());
            BigDecimal maxMark = resolveMaxMark(question, part);

            if (partMark.getObtainedMark().compareTo(maxMark) > 0) {
                throw new IllegalArgumentException("obtainedMark exceeds maxMark for question "
                        + question.getId() + " part " + part);
            }
            if (partMark.getObtainedMark().compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("obtainedMark cannot be negative");
            }

            WrittenEvaluationDetail detail = WrittenEvaluationDetail.builder()
                    .evaluation(savedEvaluation)
                    .question(question)
                    .part(part)
                    .obtainedMark(partMark.getObtainedMark())
                    .maxMark(maxMark)
                    .feedback(partMark.getFeedback())
                    .build();

            detailRepository.save(detail);
            totalMark = totalMark.add(partMark.getObtainedMark());
        }

        savedEvaluation.setTotalMark(totalMark);
        savedEvaluation.setStatus(EvaluationStatus.COMPLETED);
        savedEvaluation.setEvaluatedByAdminId(adminId);
        savedEvaluation.setEvaluatedAt(LocalDateTime.now());
        evaluationRepository.save(savedEvaluation);

        submission.setTotalObtainedMark(totalMark);
        submission.setStatus(SubmissionStatus.COMPLETED);
        submissionRepository.save(submission);

        return evaluationMapper.toResponse(savedEvaluation, detailRepository.findByEvaluationId(savedEvaluation.getId()));
    }

    private BigDecimal resolveMaxMark(WrittenQuestion question, QuestionPart part) {
        return switch (part) {
            case A -> question.getPartAMaxMark();
            case B -> question.getPartBMaxMark();
            case C -> question.getPartCMaxMark();
            case D -> question.getPartDMaxMark();
        };
    }
}
