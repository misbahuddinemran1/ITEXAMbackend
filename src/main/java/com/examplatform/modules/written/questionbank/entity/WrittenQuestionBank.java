package com.examplatform.modules.written.questionbank.entity;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "written_question_bank")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenQuestionBank {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "stimulus", columnDefinition = "TEXT", nullable = false)
    private String stimulus;

    @Column(name = "stimulus_bn", columnDefinition = "TEXT")
    private String stimulusBn;

    @Column(name = "is_board_question", nullable = false)
    @Builder.Default
    private boolean isBoardQuestion = false;

    @Column(name = "board", length = 50)
    private String board;

    @Column(name = "exam_year")
    private Integer examYear;

    // Part A
    @Column(name = "part_a_question", columnDefinition = "TEXT", nullable = false)
    private String partAQuestion;
    @Column(name = "part_a_model_answer", columnDefinition = "TEXT")
    private String partAModelAnswer;
    @Column(name = "part_a_ai_answer", columnDefinition = "TEXT")
    private String partAAiAnswer;
    @Column(name = "part_a_marking_scheme", columnDefinition = "TEXT")
    private String partAMarkingScheme;
    @Column(name = "part_a_max_mark", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal partAMaxMark = BigDecimal.valueOf(1.00);

    // Part B
    @Column(name = "part_b_question", columnDefinition = "TEXT", nullable = false)
    private String partBQuestion;
    @Column(name = "part_b_model_answer", columnDefinition = "TEXT")
    private String partBModelAnswer;
    @Column(name = "part_b_ai_answer", columnDefinition = "TEXT")
    private String partBAiAnswer;
    @Column(name = "part_b_marking_scheme", columnDefinition = "TEXT")
    private String partBMarkingScheme;
    @Column(name = "part_b_max_mark", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal partBMaxMark = BigDecimal.valueOf(2.00);

    // Part C
    @Column(name = "part_c_question", columnDefinition = "TEXT", nullable = false)
    private String partCQuestion;
    @Column(name = "part_c_model_answer", columnDefinition = "TEXT")
    private String partCModelAnswer;
    @Column(name = "part_c_ai_answer", columnDefinition = "TEXT")
    private String partCAiAnswer;
    @Column(name = "part_c_marking_scheme", columnDefinition = "TEXT")
    private String partCMarkingScheme;
    @Column(name = "part_c_max_mark", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal partCMaxMark = BigDecimal.valueOf(3.00);

    // Part D
    @Column(name = "part_d_question", columnDefinition = "TEXT", nullable = false)
    private String partDQuestion;
    @Column(name = "part_d_model_answer", columnDefinition = "TEXT")
    private String partDModelAnswer;
    @Column(name = "part_d_ai_answer", columnDefinition = "TEXT")
    private String partDAiAnswer;
    @Column(name = "part_d_marking_scheme", columnDefinition = "TEXT")
    private String partDMarkingScheme;
    @Column(name = "part_d_max_mark", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal partDMaxMark = BigDecimal.valueOf(4.00);

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

    @Transient
    public BigDecimal getTotalMaxMark() {
        return partAMaxMark.add(partBMaxMark).add(partCMaxMark).add(partDMaxMark);
    }
}
