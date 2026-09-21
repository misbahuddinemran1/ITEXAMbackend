package com.examplatform.modules.leaderboard.repository;

import com.examplatform.modules.leaderboard.entity.UserMonthlyLeaderboardStats;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserMonthlyLeaderboardStatsRepository extends JpaRepository<UserMonthlyLeaderboardStats, String> {

    Optional<UserMonthlyLeaderboardStats> findByUserIdAndYearMonth(String userId, String yearMonth);

    Page<UserMonthlyLeaderboardStats> findByEducationLevelAndYearMonthAndExamsTakenThisMonthGreaterThanEqualOrderByTotalPointsThisMonthDesc(
            String educationLevel, String yearMonth, int minExams, Pageable pageable);

    // level-wise বন্ধ থাকলে
    Page<UserMonthlyLeaderboardStats> findByYearMonthAndExamsTakenThisMonthGreaterThanEqualOrderByTotalPointsThisMonthDesc(
            String yearMonth, int minExams, Pageable pageable);
}
