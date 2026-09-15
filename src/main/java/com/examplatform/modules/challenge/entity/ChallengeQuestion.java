package com.examplatform.modules.challenge.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.guide.entity.GuidePracticeMcq;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenge_question")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeQuestion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mcq_id", nullable = false)
    private GuidePracticeMcq mcq;

    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private int orderIndex = 0;
}
