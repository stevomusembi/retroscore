package com.retroscore.repository;

import com.retroscore.entity.FootballClub;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FootballClubRepository extends JpaRepository<FootballClub, Integer> {
    Optional<FootballClub> findByName(String name);
}
