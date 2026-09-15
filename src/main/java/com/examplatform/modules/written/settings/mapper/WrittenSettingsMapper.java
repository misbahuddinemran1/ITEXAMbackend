package com.examplatform.modules.written.settings.mapper;

import com.examplatform.modules.written.settings.entity.WrittenSettings;
import com.examplatform.modules.written.settings.request.UpdateSettingsRequest;
import com.examplatform.modules.written.settings.response.SettingsResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class WrittenSettingsMapper {

    private static final Set<String> VALID_EVALUATION_MODES = Set.of("MANUAL", "AI", "HYBRID");
    private static final Set<String> VALID_SUBMISSION_TYPES = Set.of("CAMERA", "GALLERY", "PDF");
    private static final Set<String> VALID_PUBLISH_MODES = Set.of("INSTANT", "MANUAL");
    private static final Set<String> VALID_ARCHIVE_MODES = Set.of("AUTO", "DISABLE");

    public void applyUpdate(WrittenSettings settings, UpdateSettingsRequest request, String adminId) {

        if (request.getDefaultEvaluationMode() != null) {
            String mode = request.getDefaultEvaluationMode().toUpperCase();
            if (!VALID_EVALUATION_MODES.contains(mode)) {
                throw new IllegalArgumentException("Invalid defaultEvaluationMode: " + request.getDefaultEvaluationMode()
                        + ". Must be one of " + VALID_EVALUATION_MODES);
            }
            settings.setDefaultEvaluationMode(mode);
        }

        if (request.getAllowedSubmissionTypes() != null) {
            if (request.getAllowedSubmissionTypes().isEmpty()) {
                throw new IllegalArgumentException("allowedSubmissionTypes cannot be empty");
            }
            List<String> upperTypes = request.getAllowedSubmissionTypes().stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());

            for (String type : upperTypes) {
                if (!VALID_SUBMISSION_TYPES.contains(type)) {
                    throw new IllegalArgumentException("Invalid submission type: " + type
                            + ". Must be one of " + VALID_SUBMISSION_TYPES);
                }
            }
            settings.setAllowedSubmissionTypes(String.join(",", upperTypes));
        }

        if (request.getResultPublishMode() != null) {
            String mode = request.getResultPublishMode().toUpperCase();
            if (!VALID_PUBLISH_MODES.contains(mode)) {
                throw new IllegalArgumentException("Invalid resultPublishMode: " + request.getResultPublishMode()
                        + ". Must be one of " + VALID_PUBLISH_MODES);
            }
            settings.setResultPublishMode(mode);
        }

        if (request.getPracticeArchiveMode() != null) {
            String mode = request.getPracticeArchiveMode().toUpperCase();
            if (!VALID_ARCHIVE_MODES.contains(mode)) {
                throw new IllegalArgumentException("Invalid practiceArchiveMode: " + request.getPracticeArchiveMode()
                        + ". Must be one of " + VALID_ARCHIVE_MODES);
            }
            settings.setPracticeArchiveMode(mode);
        }

        settings.setUpdatedByAdminId(adminId);
    }

    public SettingsResponse toResponse(WrittenSettings settings) {
        return SettingsResponse.builder()
                .id(settings.getId())
                .defaultEvaluationMode(settings.getDefaultEvaluationMode())
                .allowedSubmissionTypes(Arrays.asList(settings.getAllowedSubmissionTypes().split(",")))
                .resultPublishMode(settings.getResultPublishMode())
                .practiceArchiveMode(settings.getPracticeArchiveMode())
                .updatedByAdminId(settings.getUpdatedByAdminId())
                .createdAt(settings.getCreatedAt())
                .updatedAt(settings.getUpdatedAt())
                .build();
    }
}
