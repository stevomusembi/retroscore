package com.retroscore.repository;

import com.retroscore.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchRepository extends JpaRepository<Match,Integer> {

}
