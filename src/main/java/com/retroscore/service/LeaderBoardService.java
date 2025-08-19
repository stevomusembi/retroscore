package com.retroscore.service;

import com.retroscore.dto.LeaderBoardEntry;
import com.retroscore.dto.LeaderBoardResponse;
import com.retroscore.dto.UserStatsWithRank;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.repository.UserGameRepository;
import com.retroscore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LeaderBoardService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserGameRepository userGameRepository;

    public LeaderBoardResponse getLeaderBoard(int page, int size){
        Pageable pageable = (Pageable) PageRequest.of(page, size,
                Sort.by("totalPoints").descending()
                        .and(Sort.by("createdAt").ascending()));

        Page<User> users = userRepository.findAllByOrderByTotalPointsDescCreatedAtAsc(pageable);

        List<LeaderBoardEntry> entries = users.getContent().stream()
                .map(this::mapToLeaderBoardEntry)
                .collect(Collectors.toList());
        return LeaderBoardResponse.builder()
                        .entries(entries)
                        .totalUsers(users.getTotalElements())
                        .currentPage(page)
                        .pageSize(size)
                        .build();
    }
public UserStatsWithRank getUserStatsWithRank(Long userId){
        User user = userRepository.findById(userId).
                orElseThrow(()-> new RuntimeException("user not found"));
        Long rank = userRepository.findUserRankByPoints(user.getTotalPoints());

        List<UserGame> userGames = userGameRepository.findByUserId(userId);

        int exactScores = (int) userGames.stream()
                .filter(ug->Boolean.TRUE.equals(ug.getIsCorrectScore()))
                .count();
        int correctResults = (int) userGames.stream()
                .filter(ug->Boolean.TRUE.equals(ug.getIsCorrectResult())&&
                        !Boolean.TRUE.equals((ug.getIsCorrectScore())))
                .count();

        return UserStatsWithRank.builder()
                .userId(userId)
                .username(user.getUsername())
                .totalPoints(user.calculateTotalPoints())
                .gamesPlayed(user.getGamesPlayed())
                .exactScorePredictions(exactScores)
                .correctResultPredictions(correctResults)
                .winPercentage(user.getWinPercentage())
                .currentRank(rank)
                .build();

}
public  LeaderBoardEntry mapToLeaderBoardEntry(User user){

    Long rank = userRepository.findUserRankByPoints(user.getTotalPoints());

        return LeaderBoardEntry.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .totalPoints(user.calculateTotalPoints())
                .gamesPlayed(user.getGamesPlayed())
                .winPercentage(user.getWinPercentage())
                .rank(rank)
                .build();
}

}
