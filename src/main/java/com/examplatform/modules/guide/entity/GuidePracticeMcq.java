package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_practice_mcq")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidePracticeMcq extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "question_text", nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(name = "question_text_bn", columnDefinition = "TEXT")
    private String questionTextBn;

    @Column(name = "is_board_question", nullable = false)
    private boolean isBoardQuestion = false;

    @Column(name = "board", length = 100)
    private String board;

    @Column(name = "year_appeared")
    private Integer yearAppeared;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @OneToMany(mappedBy = "mcq", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<GuidePracticeMcqOption> options = new ArrayList<>();
}
