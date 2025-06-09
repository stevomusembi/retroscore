package com.retroscore.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Teams")
public class FootballClub {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "stadium_name")
    private String stadiumName;

    @Column(name = "is_active")
    private boolean isActive = true;

}
