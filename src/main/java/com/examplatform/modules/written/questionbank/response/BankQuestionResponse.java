package com.examplatform.modules.written.questionbank.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankQuestionResponse {

    private String id;
    private String subjectId;
    private String subjectName;
    private String chapterId;
    private String chapterName;
    private String topicId;
    private String topicName;

    private String stimulus;
    private String stimulusBn;

    private boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    private List<BankPartResponse> parts;

    private BigDecimal totalMaxMark;
}
