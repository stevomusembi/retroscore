package com.retroscore.service;

import com.retroscore.dto.*;
import com.retroscore.entity.FootballClub;
import com.retroscore.entity.Match;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.enums.GameResult;
import com.retroscore.enums.MatchResult;
import com.retroscore.exception.MatchNotFoundException;
import com.retroscore.exception.NoMatchesFoundException;
import com.retroscore.exception.UserAlreadyPlayedException;
import com.retroscore.repository.MatchRepository;
import com.retroscore.repository.UserGameRepository;
import com.retroscore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Transactional
@Service
public class GameService {

    private final MatchRepository matchRepository;
    private final UserGameRepository userGameRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(GameService.class);
    private static final Random RANDOM = new Random();

    @Autowired
    public GameService(MatchRepository matchRepository, UserGameRepository userGameRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userGameRepository = userGameRepository;
        this.userRepository = userRepository;
    }

    public MatchDto getRandomMatch(Long userId, Long teamId, Long seasonId, String mode) {
        if (mode == null || mode.isBlank()) {
            mode = "discovery";
        }

        List<Match> matches = getFilteredMatches(userId, teamId, seasonId, mode);

        if (matches.isEmpty()) {
            throw new NoMatchesFoundException();
        }

        Match randomMatch = matches.get(RANDOM.nextInt(matches.size()));
        MatchDto matchDto = convertMatchToDto(randomMatch);

        if (userId != null) {
            enrichWithPreviousPlayHistory(matchDto, userId);
        }

        return matchDto;
    }

    private List<Match> getFilteredMatches(Long userId,Long teamId,Long seasonId,String mode){
        logger.info("Filtering matches with parameters: userId={}, teamId={}, seasonId={}, mode={}", userId, teamId, seasonId, mode);

        List<Match> matches;
        if (teamId != null && seasonId !=null){
            matches = matchRepository.findByTeamIdAndSeasonId(teamId,seasonId);
        } else if (teamId !=null){
            matches = matchRepository.findByTeamId(teamId);
        } else if (seasonId!=null) {
            matches = matchRepository.findBySeasonId(seasonId);
        } else {
            matches = matchRepository.findAll();
        }

        if(userId!= null){
            switch (mode){
                case "unplayed":
                    matches = matches.stream().filter(match -> userNotPlayedMatch(userId, match.getId())).collect(Collectors.toList());
                    break;
                case "incorrect":
                    matches = matches.stream()
                            .filter(match -> wasUserIncorrectOnMatch(userId, match.getId()))
                                    .collect(Collectors.toList());
                    break;
                case "discovery":
                    List<Match> unplayedMatches  = matches.stream().filter(match -> userNotPlayedMatch(userId, match.getId())).collect(Collectors.toList());

                    if(unplayedMatches.size()>=5){
                        matches = unplayedMatches;
                    }
                    break;
                default:
                    break;
            }
        }

        return matches;

    }

    private boolean userNotPlayedMatch(Long userId, Long matchId) {
        return userGameRepository.findByUserIdAndMatchId(userId, matchId).isEmpty();
    }

    private boolean wasUserIncorrectOnMatch(Long userId, Long matchId){
        Optional<UserGame> userGameOpt = userGameRepository.findByUserIdAndMatchId(userId, matchId);

        if (userGameOpt.isPresent()) {
            UserGame userGame = userGameOpt.get();
            // User played AND got score wrong
            return !userGame.getIsCorrectScore();
        }

        // User never played this match
        return false;
    }

    private void enrichWithPreviousPlayHistory(MatchDto matchDto, Long userId){
       userGameRepository.findByUserIdAndMatchId(userId,matchDto.getMatchId()).ifPresent(previousUserGame -> {
            PlayHistoryDto previousUserGameHistory = new PlayHistoryDto();
            previousUserGameHistory.setPreviousPlayed(true);
            previousUserGameHistory.setWasCorrectScore(previousUserGame.getIsCorrectResult());
            previousUserGameHistory.setWasCorrectResult(previousUserGame.getIsCorrectResult());
           previousUserGameHistory.setPlayedAt(previousUserGame.getPlayedAt());

            matchDto.setPlayHistoryDto(previousUserGameHistory);
        });
    }

    private MatchDto convertMatchToDto(Match match){
        MatchDto matchDto = new MatchDto();
        matchDto.setMatchId(match.getId());
        matchDto.setMatchTitle(match.getMatchTitle());
        matchDto.setSeasonName(match.getSeason().getSeasonName());
        matchDto.setHomeTeam(convertFootballClubToDto(match.getHomeTeam()));
        matchDto.setAwayTeam(convertFootballClubToDto(match.getAwayTeam()));
        matchDto.setStadiumName(match.getMatchStadiumName());
        matchDto.setHomeScore(match.getHomeScore());
        matchDto.setAwayScore(match.getAwayScore());
        matchDto.setMatchDate(match.getMatchDate());
        matchDto.setHomeCorners(match.getHomeCorners());
        matchDto.setAwayCorners(match.getAwayCorners());
        matchDto.setHomeYellowCards(match.getHomeYellowCards());
        matchDto.setAwayYellowCards(match.getAwayYellowCards());
        matchDto.setHomeRedCards(match.getHomeRedCards());
        matchDto.setAwayRedCards(match.getAwayRedCards());

        return matchDto;

    }

    private FootballClubDto convertFootballClubToDto(FootballClub club){
        if(club == null){
            return null;
        };
        FootballClubDto dto = new FootballClubDto();
        dto.setClubId(club.getId());
        dto.setName(club.getName());
        dto.setIsActive(club.isActive());
        dto.setLogoUrl(club.getLogoUrl());
        dto.setStadiumName(club.getStadiumName());

        return dto;
    }

    public UserGameResponse submitGuess(Long userId, UserGuessDto userGuess) {

        // validate user exists
        User user = userRepository.findById(userId).orElseThrow(()-> new RuntimeException("User not found"));


        // validate the match exists
        Match match = matchRepository.findById(userGuess.getMatchId()).orElseThrow(()-> new MatchNotFoundException(userGuess.getMatchId()));

        // user failed to submit a guess in time, we don't record it as a user game
        if (userGuess.getTimeIsUp() == true){
            MatchResult actualMatchResult = getMatchResultEnum(match.getHomeScore(), match.getAwayScore());
            return UserGameResponse.builder()
                    .matchId(match.getId())
                    .matchTitle(match.getMatchTitle())
                    .actualHomeScore(match.getHomeScore())
                    .actualAwayScore(match.getAwayScore())
                    .isCorrectScore(false)
                    .isCorrectResult(false)
                    .gameResult(GameResult.INCORRECT)
                    .playedAt(LocalDateTime.now())
                    .resultMessage(GameResult.TIMEUP.getMessage())
                    .userGamePoints(GameResult.TIMEUP.getPoints())
                    .actualMatchResult(actualMatchResult)
                    .build();

        }




        // check if user has played the specific match before
        Optional<UserGame> existingUserGame = userGameRepository.findByUserIdAndMatchId(userId, match.getId());
        if (existingUserGame.isPresent()){
            throw new UserAlreadyPlayedException(userId, match.getId());
        }

        logger.info("This is the submission from user={}",userGuess);
        // create new user game if it is the first time playing this match.
        UserGame userGame = new UserGame();

        userGame.setUser(user);
        userGame.setPlayedAt(LocalDateTime.now());
        userGame.setMatch(match);

        if(userGuess.getIsEasyMode() == true){
            userGame.setPredictedHomeScore(null);
            userGame.setPredictedAwayScore(null);
            userGame.setIsCorrectScore(false);

            MatchResult userSubmittedResult =  userGuess.getMatchResult();
            MatchResult actualMatchResult = getMatchResultEnum(match.getHomeScore(), match.getAwayScore());

            if(actualMatchResult == userSubmittedResult) {
                userGame.setIsCorrectResult(true);
            } else {
                userGame.setIsCorrectResult(false);
            }
        }

        if(userGuess.getIsEasyMode() != true) {
            userGame.setPredictedHomeScore(userGuess.getPredictedHomeScore());
            userGame.setPredictedAwayScore(userGuess.getPredictedAwayScore());
            //calculate other user game results
            calculateGameResult(userGame, match);
        }
        UserGame savedGame =  userGameRepository.save(userGame);

        // update user entity's stats
        updateUserEntityStats(userGame);

        return buildUserGameResponse(savedGame, match);
    }

    private void calculateGameResult(UserGame userGame, Match match){
        Integer actualHomeScore = match.getHomeScore();
        Integer actualAwayScore = match.getAwayScore();
        Integer predictedHomeScore = userGame.getPredictedHomeScore();
        Integer predictedAwayScore = userGame.getPredictedAwayScore();

        boolean isCorrectScore = actualHomeScore.equals(predictedHomeScore) && actualAwayScore.equals(predictedAwayScore);
        userGame.setIsCorrectScore(isCorrectScore);


        boolean isCorrectResult = isCorrectScore || (getMatchResult(actualHomeScore,actualAwayScore) == getMatchResult(predictedHomeScore,predictedAwayScore));
        userGame.setIsCorrectResult(isCorrectResult);

    }

    private int getMatchResult(Integer homeScore, Integer awayScore){
        return homeScore.compareTo(awayScore);
    }


    // method to update user stats after playing a game
    private void updateUserEntityStats(UserGame userGame){
        User user = userGame.getUser();
        user.setGamesPlayed(user.getGamesPlayed() +1);
        if(userGame.getIsCorrectScore()){
            user.setGamesWon(user.getGamesWon()+1);
            user.setExactScorePredictions(user.getExactScorePredictions()+1);
        } else if(userGame.getIsCorrectResult()) {
            user.setCorrectResultPredictions(user.getCorrectResultPredictions()+1);
            user.setGamesDrawn(user.getGamesDrawn()+1);
        }
        else {
                user.setGamesLost(user.getGamesLost()+1);
        }

        userRepository.save(user);
    }


    public UserDto getUserStats(Long userId){

        Optional<User> optionalUser = userRepository.findById(userId);
       if(optionalUser.isEmpty()){
           return null;
       } else {
           User user = optionalUser.get();
           Integer totalPoints = user.getGamesWon() * 3;

           UserDto userDto = new UserDto();
           userDto.setUserId(user.getId());
           userDto.setEmail(user.getEmail());
           userDto.setUsername(user.getUsername());
           userDto.setMatchesPlayed(user.getGamesPlayed());
           userDto.setWinPercentage(user.getWinPercentage());
           userDto.setMatchesPredictedWrongScore(user.getGamesLost());
           userDto.setMatchesPredictedCorrectScore(user.getGamesWon());
           userDto.setTotalPoints(totalPoints);
           userDto.setCreatedAt(user.getCreatedAt());
           userDto.setLastLoginAt(user.getLastLogin());

           return userDto;
       }
    }

    private UserGameResponse buildUserGameResponse(UserGame userGame, Match match){
        GameResult resultMessage = generateResultMessage(userGame);
        MatchResult actualMatchResult = getMatchResultEnum(match.getHomeScore(), match.getAwayScore());
        return UserGameResponse.builder()
                .userGameId(userGame.getId())
                .matchId(match.getId())
                .matchTitle(match.getMatchTitle())
                .predictedHomeScore(userGame.getPredictedHomeScore())
                .predictedAwayScore(userGame.getPredictedAwayScore())
                .actualHomeScore(match.getHomeScore())
                .actualAwayScore(match.getAwayScore())
                .isCorrectScore(userGame.getIsCorrectScore())
                .isCorrectResult(userGame.getIsCorrectResult())
                .gameResult(userGame.getGameResult())
                .playedAt(userGame.getPlayedAt())
                .resultMessage(resultMessage.getMessage())
                .userGamePoints(resultMessage.getPoints())
                .actualMatchResult(actualMatchResult)
                .build();


    }

    private MatchResult getMatchResultEnum(Integer homeScore, Integer awayScore) {
        int comparison = homeScore.compareTo(awayScore);
        if (comparison > 0) {
            return MatchResult.HOME_WIN;
        } else if (comparison < 0) {
            return MatchResult.AWAY_WIN;
        } else {
            return MatchResult.DRAW;
        }
    }

    private GameResult generateResultMessage(UserGame userGame){

        if(userGame.getIsCorrectScore()){
            return  GameResult.EXACT_SCORE;
        } else if (userGame.getIsCorrectResult()){
            return GameResult.CORRECT_RESULT;
        } else {
          return  GameResult.INCORRECT;
        }
    }

    public UserGameResponse getGameResult(Long userId, Long userGameId){
        UserGame userGame = userGameRepository.findByIdAndUserId(userGameId, userId)
                .orElseThrow(()-> new RuntimeException("UserGame not found"));
        return buildUserGameResponse(userGame,userGame.getMatch());
    }

}
