package com.retroscore.repository;

import com.retroscore.entity.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserGameRepository extends JpaRepository<UserGame, Long> {

    //check if user played a game
    Optional<UserGame> findByUserIdAndMatchId(Long userId, Long matchId);

    //find userGame by id and userid
    Optional<UserGame> findByIdAndUserId(Long id, Long userId);

}
