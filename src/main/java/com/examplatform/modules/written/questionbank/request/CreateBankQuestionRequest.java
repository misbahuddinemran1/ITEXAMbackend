package com.examplatform.modules.written.questionbank.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateBankQuestionRequest {

    private String subjectId;
    private String chapterId;
    private String topicId;

    private String stimulus;
    private String stimulusBn;

    private boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    private boolean autoGenerateAiAnswer;

    private List<BankPartRequest> parts;
}
