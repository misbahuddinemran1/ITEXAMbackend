package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "guide_practice_mcq_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidePracticeMcqOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcq_id", nullable = false)
    private GuidePracticeMcq mcq;

    @Column(name = "option_key", nullable = false, length = 5)
    private String optionKey;

    @Column(name = "option_text", nullable = false, columnDefinition = "TEXT")
    private String optionText;

    @Column(name = "option_text_bn", columnDefinition = "TEXT")
    private String optionTextBn;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect = false;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;
}
