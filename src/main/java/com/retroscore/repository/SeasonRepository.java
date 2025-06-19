package com.retroscore.repository;

import com.retroscore.entity.Season;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonRepository extends JpaRepository<Season, Integer> {

    Optional<Season> findBySeasonName(String name);
}
