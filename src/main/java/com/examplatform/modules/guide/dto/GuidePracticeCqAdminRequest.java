package com.examplatform.modules.guide.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GuidePracticeCqAdminRequest {
    private String topicId;
    private String stimulus;
    private String stimulusBn;

    @JsonProperty("isBoardQuestion")
    private boolean isBoardQuestion;

    private String board;
    private Integer examYear;

    private List<GuidePracticeCqPartItem> parts;

    // null হলে parts-এর maxMark যোগ করে নেওয়া হবে
    private Integer totalMaxMark;
    private int sortOrder;
}
