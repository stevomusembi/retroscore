package com.retroscore.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LeaderBoardResponse {
    public List<LeaderBoardEntry> entries;
    private long totalUsers;
    private int currentPage;
    private int pageSize;
}
