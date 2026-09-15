package com.examplatform.modules.written.evaluation.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WrittenLeaderboardEntryResponse {

    private int rank;
    private String userId;
    private String userName;
    private String collegeName;
    private BigDecimal obtainedMarks;
    private BigDecimal totalMarks;
    private double percentage;
    private boolean isCurrentUser;
}
