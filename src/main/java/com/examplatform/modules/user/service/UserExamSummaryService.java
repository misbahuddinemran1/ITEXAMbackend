package com.examplatform.modules.user.service;

import com.examplatform.modules.exam.entity.ExamSession;
import com.examplatform.modules.exam.repository.ExamAttemptHistoryRepository;
import com.examplatform.modules.exam.repository.ExamSessionRepository;
import com.examplatform.modules.liveexam.entity.LiveExamSession;
import com.examplatform.modules.liveexam.repository.LiveExamSessionRepository;
import com.examplatform.modules.user.dto.UserExamSummaryResponse;
import com.examplatform.modules.written.submission.enums.SubmissionStatus;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserExamSummaryService {

    private final ExamSessionRepository examSessionRepository;
    private final ExamAttemptHistoryRepository examAttemptHistoryRepository;
    private final LiveExamSessionRepository liveExamSessionRepository;
    private final WrittenSubmissionRepository writtenSubmissionRepository;

    public UserExamSummaryResponse getSummary(String userId) {
        // নিজে নিজে দেওয়া practice/mock/topic-wise ইত্যাদি (শেষ হওয়া session)
        long practice = examSessionRepository.countByUserIdAndStatusIn(
                userId,
                List.of(ExamSession.Status.COMPLETED, ExamSession.Status.TIMED_OUT));

        // Admin এর তৈরি নির্ধারিত exam (daily/weekly/live ইত্যাদি): আলাদা exam সংখ্যা ও মোট attempt
        long scheduledExams = examAttemptHistoryRepository.countDistinctExamsByUserId(userId);
        long scheduledAttempts = examAttemptHistoryRepository.countByUserId(userId);

        // এর মধ্যে Live exam কয়টা (উপসেট)
        long live = liveExamSessionRepository.countByUserIdAndStatusIn(
                userId,
                List.of(LiveExamSession.Status.SUBMITTED, LiveExamSession.Status.AUTO_SUBMITTED));

        // Written/সৃজনশীল: practice mode বাদে জমা দেওয়া submission
        long written = writtenSubmissionRepository.countByUserIdAndIsPracticeModeFalseAndStatusIn(
                userId,
                List.of(SubmissionStatus.SUBMITTED, SubmissionStatus.UNDER_REVIEW, SubmissionStatus.COMPLETED));

        return UserExamSummaryResponse.builder()
                .totalExams(practice + scheduledExams + written)
                .practiceExams(practice)
                .scheduledExams(scheduledExams)
                .scheduledAttempts(scheduledAttempts)
                .liveExams(live)
                .writtenExams(written)
                .build();
    }
}
