package com.examplatform.modules.written.submission.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StartExamRequest {
    private String examId;
    private boolean practiceMode; // true হলে practice attempt, false হলে live attempt
}