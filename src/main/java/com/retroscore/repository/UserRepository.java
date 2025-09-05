package com.retroscore.repository;

import com.retroscore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

//    Optional<User> findByUsernameAndPasswordHash(String username, String passwordHash);

    Boolean existsByUsername(String username);

    // Get top users by total points
    List<User> findTop20ByOrderByTotalPointsDescCreatedAtAsc();

    // Get user rank by points
    @Query("SELECT COUNT(u) + 1 FROM User u WHERE u.totalPoints > :userPoints")
    Long findUserRankByPoints(@Param("userPoints") Integer userPoints);

    Page<User> findAllByOrderByTotalPointsDescCreatedAtAsc(Pageable pageable);

    // Get user stats with rank
    @Query("SELECT u, " +
            "(SELECT COUNT(u2) + 1 FROM User u2 WHERE u2.totalPoints > u.totalPoints) as rank " +
            "FROM User u WHERE u.id = :userId")
    Object[] findUserWithRank(@Param("userId") Long userId);

    //find by googleId
    Optional<User> findByGoogleId(String googleId);

    //findBy email
    Optional<User> findByEmail(String email);


    Optional<User> findByUsername(String username);

    // Optional: Check if email exists (useful for validation)
    boolean existsByEmail(String email);

    // Optional: Check if Google ID exists (useful for validation)
    boolean existsByGoogleId(String googleId);
}
