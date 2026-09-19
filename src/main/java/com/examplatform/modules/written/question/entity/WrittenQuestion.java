package com.examplatform.modules.written.question.entity;

import com.examplatform.modules.taxonomy.entity.Chapter;
import com.examplatform.modules.taxonomy.entity.Subject;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "written_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WrittenQuestion {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "exam_id", nullable = false, length = 36)
    private String examId;

    // Flexible sub-questions (BCS Written স্টাইল — ২টা, ৩টা, যেকোনো সংখ্যক part হতে পারে)
    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("partOrder ASC")
    @Builder.Default
    private List<WrittenQuestionPart> parts = new ArrayList<>();

    // Knowledge Hierarchy - প্রতিটা CQ-এর exact classification
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter chapter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id")
    private Topic topic;

    @Column(name = "question_order", nullable = false)
    @Builder.Default
    private Integer questionOrder = 1;

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
    @Column(name = "part_a_question", columnDefinition = "TEXT")
    private String partAQuestion; // Deprecated: পুরনো HSC-স্টাইল ৪-part প্রশ্নের জন্য, নতুন প্রশ্নে `parts` ব্যবহার হয়
    @Column(name = "part_a_model_answer", columnDefinition = "TEXT")
    private String partAModelAnswer;
    @Column(name = "part_a_ai_answer", columnDefinition = "TEXT")
    private String partAAiAnswer;
    @Column(name = "part_a_marking_scheme", columnDefinition = "TEXT")
    private String partAMarkingScheme;
    @Column(name = "part_a_max_mark", precision = 5, scale = 2)
    private BigDecimal partAMaxMark;

    // Part B
    @Column(name = "part_b_question", columnDefinition = "TEXT")
    private String partBQuestion; // Deprecated
    @Column(name = "part_b_model_answer", columnDefinition = "TEXT")
    private String partBModelAnswer;
    @Column(name = "part_b_ai_answer", columnDefinition = "TEXT")
    private String partBAiAnswer;
    @Column(name = "part_b_marking_scheme", columnDefinition = "TEXT")
    private String partBMarkingScheme;
    @Column(name = "part_b_max_mark", precision = 5, scale = 2)
    private BigDecimal partBMaxMark;

    // Part C
    @Column(name = "part_c_question", columnDefinition = "TEXT")
    private String partCQuestion; // Deprecated
    @Column(name = "part_c_model_answer", columnDefinition = "TEXT")
    private String partCModelAnswer;
    @Column(name = "part_c_ai_answer", columnDefinition = "TEXT")
    private String partCAiAnswer;
    @Column(name = "part_c_marking_scheme", columnDefinition = "TEXT")
    private String partCMarkingScheme;
    @Column(name = "part_c_max_mark", precision = 5, scale = 2)
    private BigDecimal partCMaxMark;

    // Part D
    @Column(name = "part_d_question", columnDefinition = "TEXT")
    private String partDQuestion; // Deprecated
    @Column(name = "part_d_model_answer", columnDefinition = "TEXT")
    private String partDModelAnswer;
    @Column(name = "part_d_ai_answer", columnDefinition = "TEXT")
    private String partDAiAnswer;
    @Column(name = "part_d_marking_scheme", columnDefinition = "TEXT")
    private String partDMarkingScheme;
    @Column(name = "part_d_max_mark", precision = 5, scale = 2)
    private BigDecimal partDMaxMark;

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

    // ---- Flexible-part lookups by 1-based partOrder, with legacy A/B/C/D fallback ----
    // ব্যবহার হয় Evaluation/Transcription মডিউলে, যাতে ওগুলো fixed A/B/C/D-এর বদলে
    // যেকোনো সংখ্যক flexible part নিয়ে কাজ করতে পারে।

    @Transient
    public int getPartCount() {
        if (parts != null && !parts.isEmpty()) return parts.size();
        return 4; // legacy fixed-4 প্রশ্ন
    }

    @Transient
    public String getPartQuestionText(int partOrder) {
        if (parts != null && !parts.isEmpty()) {
            return parts.stream().filter(p -> p.getPartOrder() == partOrder)
                    .map(WrittenQuestionPart::getQuestionText).findFirst().orElse(null);
        }
        return switch (partOrder) {
            case 1 -> partAQuestion;
            case 2 -> partBQuestion;
            case 3 -> partCQuestion;
            case 4 -> partDQuestion;
            default -> null;
        };
    }

    @Transient
    public String getPartModelAnswer(int partOrder) {
        if (parts != null && !parts.isEmpty()) {
            return parts.stream().filter(p -> p.getPartOrder() == partOrder)
                    .map(WrittenQuestionPart::getModelAnswer).findFirst().orElse(null);
        }
        return switch (partOrder) {
            case 1 -> partAModelAnswer;
            case 2 -> partBModelAnswer;
            case 3 -> partCModelAnswer;
            case 4 -> partDModelAnswer;
            default -> null;
        };
    }

    @Transient
    public String getPartAiAnswer(int partOrder) {
        if (parts != null && !parts.isEmpty()) {
            return parts.stream().filter(p -> p.getPartOrder() == partOrder)
                    .map(WrittenQuestionPart::getAiAnswer).findFirst().orElse(null);
        }
        return switch (partOrder) {
            case 1 -> partAAiAnswer;
            case 2 -> partBAiAnswer;
            case 3 -> partCAiAnswer;
            case 4 -> partDAiAnswer;
            default -> null;
        };
    }

    @Transient
    public BigDecimal getPartMaxMark(int partOrder) {
        if (parts != null && !parts.isEmpty()) {
            return parts.stream().filter(p -> p.getPartOrder() == partOrder)
                    .map(WrittenQuestionPart::getMaxMark).findFirst().orElse(BigDecimal.ZERO);
        }
        BigDecimal v = switch (partOrder) {
            case 1 -> partAMaxMark;
            case 2 -> partBMaxMark;
            case 3 -> partCMaxMark;
            case 4 -> partDMaxMark;
            default -> null;
        };
        return v != null ? v : BigDecimal.ZERO;
    }

    @Transient
    public BigDecimal getTotalMaxMark() {
        if (parts != null && !parts.isEmpty()) {
            return parts.stream()
                    .map(WrittenQuestionPart::getMaxMark)
                    .filter(java.util.Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        // Legacy fallback: পুরনো fixed A/B/C/D প্রশ্নের জন্য
        BigDecimal a = partAMaxMark != null ? partAMaxMark : BigDecimal.ZERO;
        BigDecimal b = partBMaxMark != null ? partBMaxMark : BigDecimal.ZERO;
        BigDecimal c = partCMaxMark != null ? partCMaxMark : BigDecimal.ZERO;
        BigDecimal d = partDMaxMark != null ? partDMaxMark : BigDecimal.ZERO;
        return a.add(b).add(c).add(d);
    }
}
