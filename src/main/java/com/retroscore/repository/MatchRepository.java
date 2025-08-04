package com.retroscore.repository;

import com.retroscore.entity.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match,Long> {

    @Query("SELECT m FROM Match m WHERE m.homeTeam.id = :teamId OR m.awayTeam.id= :teamId")
    List<Match> findByTeamId(@Param("teamId") Long teamId);
    List<Match> findBySeasonId(Long seasonId);
    @Query("SELECT m FROM Match m WHERE (m.homeTeam.id = :teamId OR m.awayTeam.id= :teamId) AND m.season.id = :seasonId")
    List<Match> findByTeamIdAndSeasonId(Long teamId, Long seasonId);
}
