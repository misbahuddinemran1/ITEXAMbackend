package com.examplatform.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * একজন ছাত্র মোট কতগুলো পরীক্ষা দিয়েছে তার সারসংক্ষেপ।
 * Admin (user profile) ও Student app (নিজের হিসাব) দুই জায়গাতেই ব্যবহার হয়।
 *
 * totalExams = practiceExams + scheduledExams + writtenExams
 * liveExams শুধু scheduledExams এর একটা উপসেট (আলাদা করে যোগ করা হয় না)।
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserExamSummaryResponse {
    private long totalExams;
    private long practiceExams;
    private long scheduledExams;
    private long scheduledAttempts;
    private long liveExams;
    private long writtenExams;
}
