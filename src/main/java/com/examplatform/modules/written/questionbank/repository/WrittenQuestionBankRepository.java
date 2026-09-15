package com.examplatform.modules.written.questionbank.repository;

import com.examplatform.modules.written.questionbank.entity.WrittenQuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WrittenQuestionBankRepository extends JpaRepository<WrittenQuestionBank, String> {

    List<WrittenQuestionBank> findBySubjectId(String subjectId);

    List<WrittenQuestionBank> findBySubjectIdAndChapterId(String subjectId, String chapterId);

    List<WrittenQuestionBank> findByIsBoardQuestionTrue();

    List<WrittenQuestionBank> findByBoardAndExamYear(String board, Integer examYear);
}
