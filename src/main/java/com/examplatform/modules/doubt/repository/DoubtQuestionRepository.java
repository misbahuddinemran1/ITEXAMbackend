package com.examplatform.modules.doubt.repository;

import com.examplatform.modules.doubt.entity.DoubtQuestion;
import com.examplatform.modules.doubt.enums.DoubtStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoubtQuestionRepository extends JpaRepository<DoubtQuestion, String> {

    List<DoubtQuestion> findByStudentUserIdOrderByCreatedAtDesc(String studentUserId);

    List<DoubtQuestion> findByStatusOrderByCreatedAtAsc(DoubtStatus status);

    List<DoubtQuestion> findByStatusAndChapterIdOrderByCreatedAtDesc(DoubtStatus status, String chapterId);

    List<DoubtQuestion> findByStatusOrderByCreatedAtDesc(DoubtStatus status);
}
