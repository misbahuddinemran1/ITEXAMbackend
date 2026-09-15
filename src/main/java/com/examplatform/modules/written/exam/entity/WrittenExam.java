package com.examplatform.modules.written.exam.entity;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import com.examplatform.modules.written.exam.enums.AiProvider;
import com.examplatform.modules.written.exam.enums.EvaluationMode;
import com.examplatform.modules.written.exam.enums.ExamStatus;
import com.examplatform.modules.written.exam.enums.PartEvaluationMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "written_exam")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenExam {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "title_bn", length = 200)
    private String titleBn;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "education_level", length = 10)
    private String educationLevel;

    // Knowledge Hierarchy (MCQ module-এর মতো)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "total_marks", nullable = false)
    @Builder.Default
    private Integer totalMarks = 0;

    @Column(name = "duration_minutes", nullable = false)
    @Builder.Default
    private Integer durationMinutes = 0;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "cycle_number", nullable = false)
    @Builder.Default
    private Integer cycleNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private ExamStatus status = ExamStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "evaluation_mode", nullable = false, length = 10)
    @Builder.Default
    private EvaluationMode evaluationMode = EvaluationMode.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "ai_provider")
    private AiProvider aiProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_a_mode", nullable = false)
    @Builder.Default
    private PartEvaluationMode partAMode = PartEvaluationMode.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_b_mode", nullable = false)
    @Builder.Default
    private PartEvaluationMode partBMode = PartEvaluationMode.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_c_mode", nullable = false)
    @Builder.Default
    private PartEvaluationMode partCMode = PartEvaluationMode.MANUAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_d_mode", nullable = false)
    @Builder.Default
    private PartEvaluationMode partDMode = PartEvaluationMode.MANUAL;


    @Column(name = "practice_enabled", nullable = false)
    @Builder.Default
    private Boolean practiceEnabled = true;

    @Column(name = "show_result_in_practice", nullable = false)
    @Builder.Default
    private Boolean showResultInPractice = false;
    @Column(name = "created_by_admin_id", length = 36)
    private String createdByAdminId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
