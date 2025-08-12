package com.retroscore.repository;

import com.retroscore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndPasswordHash(String username, String passwordHash);

    Boolean existsByUsername(String username);

}
