package com.examplatform.modules.written.questionbank.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttachToExamRequest {
    private String examId;
    private List<String> bankQuestionIds; // একসাথে একাধিক bank question exam এ যুক্ত করার জন্য
}
