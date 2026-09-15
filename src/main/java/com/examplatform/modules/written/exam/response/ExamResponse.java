package com.examplatform.modules.written.exam.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResponse {

    private String id;
    private String title;
    private String titleBn;
    private String description;
    private String educationLevel;

    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;

    private Integer totalMarks;
    private Integer durationMinutes;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer cycleNumber;
    private String status;
    private String evaluationMode;

    private String aiProvider;

    // Only meaningful when evaluationMode = HYBRID
    private String partAMode;
    private String partBMode;
    private String partCMode;
    private String partDMode;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean practiceEnabled;
    private Boolean showResultInPractice;
}
