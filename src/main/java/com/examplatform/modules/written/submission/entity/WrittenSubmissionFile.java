package com.examplatform.modules.written.submission.entity;

import com.examplatform.modules.written.submission.enums.FileType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "written_submission_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenSubmissionFile {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "submission_id", nullable = false, length = 36)
    private String submissionId;

    @Column(name = "page_number", nullable = false)
    @Builder.Default
    private Integer pageNumber = 1;

    @Column(name = "file_url", length = 500)
    private String fileUrl;

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 10)
    @Builder.Default
    private FileType fileType = FileType.IMAGE;

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        uploadedAt = LocalDateTime.now();
    }
}