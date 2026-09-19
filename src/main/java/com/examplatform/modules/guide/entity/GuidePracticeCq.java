package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guide_practice_cq")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GuidePracticeCq extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;

    @Column(name = "stimulus", nullable = false, columnDefinition = "TEXT")
    private String stimulus;

    @Column(name = "stimulus_bn", columnDefinition = "TEXT")
    private String stimulusBn;

    @Column(name = "is_board_question", nullable = false)
    private boolean isBoardQuestion = false;

    @Column(name = "board", length = 100)
    private String board;

    @Column(name = "exam_year")
    private Integer examYear;

    @OneToMany(mappedBy = "cq", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("partOrder ASC")
    @Builder.Default
    private List<GuidePracticeCqPart> parts = new ArrayList<>();

    @Column(name = "total_max_mark")
    private Integer totalMaxMark;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
