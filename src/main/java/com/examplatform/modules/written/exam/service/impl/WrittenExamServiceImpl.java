package com.examplatform.modules.written.exam.service.impl;

import com.examplatform.modules.written.exam.entity.WrittenExam;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.mapper.WrittenExamMapper;
import com.examplatform.modules.written.exam.repository.WrittenExamRepository;
import com.examplatform.modules.written.exam.request.CreateExamRequest;
import com.examplatform.modules.written.exam.request.ReopenExamRequest;
import com.examplatform.modules.written.exam.request.UpdateExamRequest;
import com.examplatform.modules.written.exam.response.ExamResponse;
import com.examplatform.modules.written.exam.response.ExamSummaryResponse;
import com.examplatform.modules.written.exam.service.WrittenExamService;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenExamServiceImpl implements WrittenExamService {

    private final WrittenExamRepository examRepository;
    private final WrittenExamMapper examMapper;
    private final WrittenSubmissionRepository submissionRepository; // used to check already-attempted flag

    @Override
    @Transactional
    public ExamResponse createExam(CreateExamRequest request, String adminId) {
        WrittenExam exam = examMapper.toEntity(request);
        exam.setCreatedByAdminId(adminId);
        WrittenExam saved = examRepository.save(exam);
        return examMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(String examId, UpdateExamRequest request) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() == ExamStatus.LIVE) {
            throw new IllegalStateException("Cannot update an exam while it is LIVE");
        }

        examMapper.applyUpdate(exam, request);
        WrittenExam updated = examRepository.save(exam);
        return examMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public ExamResponse publishExam(String examId) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() != ExamStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT exams can be published");
        }

        exam.setStatus(ExamStatus.PUBLISHED);
        return examMapper.toResponse(examRepository.save(exam));
    }

    /**
     * Moves an exam to LIVE. Supports two flows from the admin's perspective:
     *  - PUBLISHED -> LIVE: normal first-time launch of a newly created exam.
     *  - ENDED -> LIVE: re-exam — admin has already updated startTime/endTime via
     *    updateExam() to a new window, and now wants to reopen the exam for that window
     *    WITHOUT bumping cycleNumber. Students who already attempted in this same cycle
     *    remain blocked (checked via cycleNumber match in submission lookups), while
     *    students who haven't attempted yet can now take it in the new time window.
     *    This is intentionally distinct from reopenExam(), which increments cycleNumber
     *    and allows everyone (including previous takers) to attempt again.
     */
    @Override
    @Transactional
    public ExamResponse goLive(String examId) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() != ExamStatus.PUBLISHED && exam.getStatus() != ExamStatus.ENDED) {
            throw new IllegalStateException("Only PUBLISHED or ENDED exams can go LIVE");
        }

        exam.setStatus(ExamStatus.LIVE);
        return examMapper.toResponse(examRepository.save(exam));
    }

    @Override
    @Transactional
    public ExamResponse endExam(String examId) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() != ExamStatus.LIVE) {
            throw new IllegalStateException("Only LIVE exams can be ended");
        }

        exam.setStatus(ExamStatus.ENDED);
        return examMapper.toResponse(examRepository.save(exam));
    }

    @Override
    @Transactional
    public ExamResponse archiveExam(String examId) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() != ExamStatus.ENDED) {
            throw new IllegalStateException("Only ENDED exams can be archived");
        }

        exam.setStatus(ExamStatus.ARCHIVED);
        return examMapper.toResponse(examRepository.save(exam));
    }

    /**
     * Admin re-opens an ENDED (or ARCHIVED) exam.
     * Increments cycle_number -> everyone (previous takers + new takers) can attempt again.
     * Previous cycle's submissions/evaluations remain untouched (historical record).
     */
    @Override
    @Transactional
    public ExamResponse reopenExam(String examId, ReopenExamRequest request) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() != ExamStatus.ENDED && exam.getStatus() != ExamStatus.ARCHIVED) {
            throw new IllegalStateException("Only ENDED or ARCHIVED exams can be reopened");
        }

        exam.setCycleNumber(exam.getCycleNumber() + 1);
        exam.setStatus(ExamStatus.LIVE);

        if (request.getNewStartTime() != null) {
            exam.setStartTime(request.getNewStartTime());
        }
        if (request.getNewEndTime() != null) {
            exam.setEndTime(request.getNewEndTime());
        }

        return examMapper.toResponse(examRepository.save(exam));
    }

    @Override
    public ExamResponse getExamById(String examId) {
        return examMapper.toResponse(getExamOrThrow(examId));
    }

    // Hybrid filter: status=LIVE এবং এখনকার সময় start-end window এর মধ্যে থাকলেই দেখাবে
    @Override
    public List<ExamSummaryResponse> getLiveExamsForStudent(String userId, String educationLevel) {
        LocalDateTime now = LocalDateTime.now();
        List<WrittenExam> liveExams = examRepository
                .findByEducationLevelAndStatusAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
                        educationLevel, ExamStatus.LIVE, now, now);

        return liveExams.stream()
                .map(exam -> {
                    boolean alreadyAttempted = submissionRepository
                            .existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalseAndStatusIn(
                                    exam.getId(), userId, exam.getCycleNumber(),
                                    List.of(
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.SUBMITTED,
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.UNDER_REVIEW,
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.COMPLETED));
                    return examMapper.toSummaryResponse(exam, alreadyAttempted);
                })
                .toList();
    }

    // status=LIVE কিন্তু endTime পার হয়ে গেছে এমন exam — সমাপ্ত/প্র্যাকটিস স্ক্রিনের জন্য
    @Override
    public List<ExamSummaryResponse> getFinishedExamsForStudent(String userId, String educationLevel) {
        LocalDateTime now = LocalDateTime.now();
        List<WrittenExam> finishedExams = examRepository
                .findByEducationLevelAndStatusAndEndTimeBefore(educationLevel, ExamStatus.LIVE, now);

        return finishedExams.stream()
                .map(exam -> {
                    boolean alreadyAttempted = submissionRepository
                            .existsByExamIdAndUserIdAndCycleNumberAndIsPracticeModeFalseAndStatusIn(
                                    exam.getId(), userId, exam.getCycleNumber(),
                                    List.of(
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.SUBMITTED,
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.UNDER_REVIEW,
                                            com.examplatform.modules.written.submission.enums.SubmissionStatus.COMPLETED));
                    return examMapper.toSummaryResponse(exam, alreadyAttempted);
                })
                .toList();
    }

    @Override
    public List<ExamResponse> getAllExamsForAdmin() {
        return examRepository.findAll().stream()
                .map(examMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteExam(String examId) {
        WrittenExam exam = getExamOrThrow(examId);

        if (exam.getStatus() == ExamStatus.LIVE) {
            throw new IllegalStateException("Cannot delete a LIVE exam");
        }

        examRepository.delete(exam);
    }

    private WrittenExam getExamOrThrow(String examId) {
        return examRepository.findById(examId)
                .orElseThrow(() -> new NoSuchElementException("Exam not found: " + examId));
    }
}
