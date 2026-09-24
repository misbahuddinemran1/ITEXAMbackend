package com.examplatform.modules.liveexam.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class RevisionSubmitRequest {
    private List<String> questionIds;
    private Map<String, String> answers;
}
