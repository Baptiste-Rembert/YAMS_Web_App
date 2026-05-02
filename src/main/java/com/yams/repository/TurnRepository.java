package com.yams.repository;

import com.yams.model.Turn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TurnRepository extends JpaRepository<Turn, Long> {

	java.util.Optional<Turn> findFirstByGameIdAndPlayerIdAndCompletedFalse(Long gameId, Long playerId);

	java.util.List<Turn> findByGameId(Long gameId);

	java.util.Optional<Turn> findTopByGameIdAndPlayerIdOrderByIdDesc(Long gameId, Long playerId);
}
