package com.retroscore.entity;

import com.retroscore.enums.GameDifficulty;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
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
    private String passwordHash;

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

    @Column(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    @Column(name = "match_reminders")
    private boolean matchReminders= true;

    @Column(name = "score_updates")
    private boolean scoreUpdates = true;

    @Column(name = "preferred_league")
    private String preferredLeague = "ALL";

    @Enumerated(EnumType.STRING)
    @Column(name = "game_difficulty")
    private GameDifficulty gameDifficulty = GameDifficulty.MEDIUM;

    @Column(name = "show_hints")
    private  boolean showHints = true;

    @Column(name = "time_limit")
    private int timeLimit = 5;

    public User() {
    }

    public User(String username, String email, String passwordHash) {
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
        this.lastLogin = LocalDateTime.now();

    }
}
