package com.examplatform.modules.leaderboard.dto;

import com.examplatform.modules.leaderboard.entity.LeaderboardSettings;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardSettingsUpdateRequest {
    private int overallMinExamsRequired;
    private LeaderboardSettings.ThresholdType monthlyThresholdType;
    private int monthlyMinExamsRequired;
    private int monthlyAllowedMissedExams;
    private boolean levelWiseSeparate;
    private boolean enabled;
}
