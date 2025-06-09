package com.retroscore.repository;

import com.retroscore.entity.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGameRepository extends JpaRepository<UserGame, Integer> {
    
}
