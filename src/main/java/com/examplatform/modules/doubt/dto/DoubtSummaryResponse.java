package com.examplatform.modules.doubt.dto;

import com.examplatform.modules.doubt.enums.DoubtStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DoubtSummaryResponse {
    private String id;
    private String chapterId;
    private String questionText; // preview/truncated হতে পারে UI তে
    private boolean hasImage;
    private boolean hasPdf;
    private DoubtStatus status;
    private LocalDateTime createdAt;
}
