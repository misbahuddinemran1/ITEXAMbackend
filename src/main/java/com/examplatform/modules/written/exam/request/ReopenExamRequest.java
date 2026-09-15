package com.examplatform.modules.written.exam.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Admin uses this to re-open an ENDED exam.
 * Increments cycle_number so previous & new takers can attempt again.
 */
@Getter
@Setter
public class ReopenExamRequest {

    private LocalDateTime newStartTime;

    private LocalDateTime newEndTime;
}