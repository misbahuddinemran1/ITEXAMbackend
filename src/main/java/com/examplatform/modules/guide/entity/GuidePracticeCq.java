package com.examplatform.modules.guide.entity;

import com.examplatform.common.entity.BaseEntity;
import com.examplatform.modules.taxonomy.entity.Topic;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "part_a_question", columnDefinition = "TEXT")
    private String partAQuestion;
    @Column(name = "part_a_model_answer", columnDefinition = "TEXT")
    private String partAModelAnswer;
    @Column(name = "part_a_marking_scheme", columnDefinition = "TEXT")
    private String partAMarkingScheme;
    @Column(name = "part_a_max_mark")
    private Integer partAMaxMark;

    @Column(name = "part_b_question", columnDefinition = "TEXT")
    private String partBQuestion;
    @Column(name = "part_b_model_answer", columnDefinition = "TEXT")
    private String partBModelAnswer;
    @Column(name = "part_b_marking_scheme", columnDefinition = "TEXT")
    private String partBMarkingScheme;
    @Column(name = "part_b_max_mark")
    private Integer partBMaxMark;

    @Column(name = "part_c_question", columnDefinition = "TEXT")
    private String partCQuestion;
    @Column(name = "part_c_model_answer", columnDefinition = "TEXT")
    private String partCModelAnswer;
    @Column(name = "part_c_marking_scheme", columnDefinition = "TEXT")
    private String partCMarkingScheme;
    @Column(name = "part_c_max_mark")
    private Integer partCMaxMark;

    @Column(name = "part_d_question", columnDefinition = "TEXT")
    private String partDQuestion;
    @Column(name = "part_d_model_answer", columnDefinition = "TEXT")
    private String partDModelAnswer;
    @Column(name = "part_d_marking_scheme", columnDefinition = "TEXT")
    private String partDMarkingScheme;
    @Column(name = "part_d_max_mark")
    private Integer partDMaxMark;

    @Column(name = "total_max_mark")
    private Integer totalMaxMark;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}
