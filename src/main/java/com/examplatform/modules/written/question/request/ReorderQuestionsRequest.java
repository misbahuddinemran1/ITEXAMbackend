package com.examplatform.modules.written.question.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Admin can reorder all questions of an exam in one call.
 * List of {questionId, newOrder}
 */
@Getter
@Setter
public class ReorderQuestionsRequest {

    private List<QuestionOrderItem> items;

    @Getter
    @Setter
    public static class QuestionOrderItem {
        private String questionId;
        private Integer newOrder;
    }
}