package com.examplatform.modules.exam.dto.response;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamListResponse {

    private String id;
    private String name;
    private String examCode;
    private String examType;
    private String publishStatus;

    private int totalQuestions;
    private double totalMarks;
    private int durationMinutes;

    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private String attemptsAllowed;
    private boolean isPremiumOnly;

    private long totalAttempts; // কতজন দিয়েছে

    // exam-category স্ক্রিনে (frontend) client-side filter এর জন্য দরকার —
    // আগে এই field না থাকায় category-ভিত্তিক স্ক্রিনে সব exam বাদ পড়ে যেত
    private List<String> targetLevels;
}