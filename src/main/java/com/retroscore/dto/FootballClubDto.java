package com.retroscore.dto;

import lombok.Data;

@Data
public class FootballClubDto {
    private Long clubId;
    private String name;
    private String logoUrl;
    private  String stadiumName;
    private Boolean isActive;
}
