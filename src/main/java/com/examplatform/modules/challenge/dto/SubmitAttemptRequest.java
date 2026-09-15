package com.examplatform.modules.challenge.dto;

import lombok.Data;

@Data
public class SubmitAttemptRequest {
    private String mcqId;
    private String selectedOptionId;   // null হলে skip ধরা হবে
    private Integer timeTakenMs;
}
