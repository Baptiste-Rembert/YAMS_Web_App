package com.yams.repository;

import com.yams.model.Scorecard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScorecardRepository extends JpaRepository<Scorecard, Long> {
}
