package com.examplatform.modules.guide.repository;

import com.examplatform.modules.guide.entity.GuidePracticeMcqOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuidePracticeMcqOptionRepository extends JpaRepository<GuidePracticeMcqOption, String> {

    List<GuidePracticeMcqOption> findAllByMcqIdOrderByOrderIndexAsc(String mcqId);

    void deleteAllByMcqId(String mcqId);
}
