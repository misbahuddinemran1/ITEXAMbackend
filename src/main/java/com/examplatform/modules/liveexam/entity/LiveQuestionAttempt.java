package com.examplatform.modules.liveexam.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "live_question_attempts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveQuestionAttempt {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "session_id", nullable = false, length = 36)
    private String sessionId;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    @Column(name = "question_id", nullable = false, length = 36)
    private String questionId;

    @Column(name = "selected_option_id", length = 36)
    private String selectedOptionId;

    @Column(name = "is_correct", nullable = false)
    @Builder.Default
    private boolean isCorrect = false;

    @Column(name = "is_skipped", nullable = false)
    @Builder.Default
    private boolean isSkipped = false;

    @Column(name = "answered_at", nullable = false)
    private LocalDateTime answeredAt;

    @PrePersist
    protected void onCreate() {
        if (answeredAt == null) {
            answeredAt = LocalDateTime.now();
        }
    }
}
