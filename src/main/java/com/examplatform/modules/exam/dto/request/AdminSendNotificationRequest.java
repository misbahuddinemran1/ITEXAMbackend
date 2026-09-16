package com.examplatform.modules.exam.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSendNotificationRequest {
    private NotificationTargetType targetType;
    private String educationLevel; // targetType = EDUCATION_LEVEL হলে দরকার (HONOURS/ENGINEERING/DEGREE/MASTERS/DIPLOMA/OTHER)
    private String targetExam;     // targetType = TARGET_EXAM হলে দরকার (BCS_ICT/NTRCA_ICT/BANK_IT/GOVT_IT/OTHER)
    private String examId;         // targetType = WRITTEN_EXAM বা MCQ_EXAM হলে দরকার
    private String title;
    private String body;
    private String deleteAt; // ISO date string, optional, e.g. "2026-07-20T00:00:00"
}
