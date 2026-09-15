package com.examplatform.modules.written.submission.entity;

import com.examplatform.modules.written.question.entity.WrittenQuestion;
import com.examplatform.modules.written.question.enums.QuestionPart;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "written_submission_transcript")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenSubmissionTranscript {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "submission_id", nullable = false, length = 36)
    private String submissionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private WrittenQuestion question;

    @Enumerated(EnumType.STRING)
    @Column(name = "part", nullable = false, length = 1)
    private QuestionPart part;

    @Column(name = "transcribed_text", columnDefinition = "TEXT")
    private String transcribedText;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
    }
}