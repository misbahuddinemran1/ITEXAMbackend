package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateQuestionRequest {

    private String subjectId;
    private String chapterId;
    private String topicId;

    private Integer questionOrder;
    private String stimulus;
    private String stimulusBn;

    private Boolean isBoardQuestion;
    private String board;
    private Integer examYear;

    // null পাঠালে parts অপরিবর্তিত থাকবে; non-null পাঠালে পুরনো সব part বাদ দিয়ে এগুলো বসবে
    private List<QuestionPartRequest> parts;
}
