package com.yams.repository;

import com.yams.model.Scorecard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {
    Optional<Scorecard> findByGameIdAndPlayerId(Long gameId, Long playerId);
    List<Scorecard> findByGameId(Long gameId);
}
