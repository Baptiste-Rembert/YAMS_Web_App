package com.yams.repository;

import com.yams.model.GameEventLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameEventLogRepository extends JpaRepository<GameEventLog, Long> {

    Page<GameEventLog> findByGameIdOrderByIdDesc(Long gameId, Pageable pageable);
}