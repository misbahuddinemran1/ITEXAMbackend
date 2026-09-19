package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guide_practice_cq_part")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidePracticeCqPart extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cq_id", nullable = false)
    private GuidePracticeCq cq;

    @Column(name = "part_order", nullable = false)
    private Integer partOrder;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "model_answer", columnDefinition = "TEXT")
    private String modelAnswer;

    @Column(name = "marking_scheme", columnDefinition = "TEXT")
    private String markingScheme;

    @Column(name = "max_mark")
    private Integer maxMark;
}
