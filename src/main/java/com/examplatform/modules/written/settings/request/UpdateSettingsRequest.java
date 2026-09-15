package com.examplatform.modules.written.settings.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateSettingsRequest {

    private String defaultEvaluationMode; // MANUAL / AI / HYBRID

    private List<String> allowedSubmissionTypes; // subset of CAMERA, GALLERY, PDF

    private String resultPublishMode; // INSTANT / MANUAL

    private String practiceArchiveMode; // AUTO / DISABLE
}
