package com.examplatform.modules.written.settings.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettingsResponse {

    private String id;
    private String defaultEvaluationMode;
    private List<String> allowedSubmissionTypes;
    private String resultPublishMode;
    private String practiceArchiveMode;
    private String updatedByAdminId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
