package com.examplatform.modules.challenge.repository;

import com.examplatform.modules.challenge.entity.UserChallengeStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserChallengeStatsRepository extends JpaRepository<UserChallengeStats, String> {
    Optional<UserChallengeStats> findByUserId(String userId);

    @Query("SELECT s FROM UserChallengeStats s ORDER BY s.totalPoints DESC")
    List<UserChallengeStats> findLeaderboard(org.springframework.data.domain.Pageable pageable);
}
