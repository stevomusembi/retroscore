package com.retroscore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "games_played", nullable = false)
    private Integer gamesPlayed = 0;

    @Column(name = "games_won", nullable = false)
    private Integer gamesWon = 0;

    @Column(name = "games_lost", nullable = false)
    private  Integer gamesLost = 0;

    //Win percentage
    public  Double getWinPercentage(){
        return gamesPlayed > 0 ? (gamesWon * 100)/ gamesPlayed : 0.0;
    }

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;
}
