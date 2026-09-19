package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateQuestionRequest {

    private String examId;

    private String subjectId;   // required
    private String chapterId;   // required
    private String topicId;     // optional

    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    // ---- Board / Previous-year info ----
    private boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    // ---- create করার সময়ই AI answer বানিয়ে নেবে কিনা ----
    private boolean autoGenerateAiAnswer;

    // ---- Flexible sub-questions (BCS Written স্টাইল, ২/৩/যেকোনো সংখ্যক part) ----
    private List<QuestionPartRequest> parts;
}
