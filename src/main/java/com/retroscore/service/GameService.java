package com.retroscore.service;

import com.retroscore.dto.FootballClubDto;
import com.retroscore.dto.MatchDto;
import com.retroscore.dto.UserGameResponse;
import com.retroscore.dto.UserGuessDto;
import com.retroscore.entity.FootballClub;
import com.retroscore.entity.Match;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.exception.MatchNotFoundException;
import com.retroscore.exception.UserAlreadyPlayedException;
import com.retroscore.repository.MatchRepository;
import com.retroscore.repository.UserGameRepository;
import com.retroscore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Transactional
@Service
public class GameService {

    private  MatchRepository matchRepository;
    private UserGameRepository userGameRepository;
    private UserRepository userRepository;

    @Autowired
    public GameService(MatchRepository matchRepository, UserGameRepository userGameRepository, UserRepository userRepository) {
        this.matchRepository = matchRepository;
        this.userGameRepository = userGameRepository;
        this.userRepository = userRepository;
    }

    public MatchDto getRandomMatch(Long teamId) {
        List<Match> matches;

        if(teamId != null){
            matches = matchRepository.findByTeamId(teamId);
        } else{
            matches = matchRepository.findAll();
        }

        if(matches.isEmpty()){
            return null;
        }

        Random random =  new Random();
        Match randomMatch= matches.get(random.nextInt(matches.size()));
        return convertMatchToDto(randomMatch);

    }
    private MatchDto convertMatchToDto(Match match){
        MatchDto matchDto = new MatchDto();
        matchDto.setMatchId(match.getId());
        matchDto.setMatchTitle(match.getMatchTitle());
        matchDto.setHomeTeam(convertFootballClubToDto(match.getHomeTeam()));
        matchDto.setAwayTeam(convertFootballClubToDto(match.getAwayTeam()));
        matchDto.setStadiumName(match.getMatchStadiumName());
//        matchDto.setHomeScore(match.getHomeScore());
//        matchDto.setAwayScore(match.getAwayScore());
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
        Match match = matchRepository.findById(Math.toIntExact(userGuess.getMatchId())).orElseThrow(()-> new MatchNotFoundException(userGuess.getMatchId()));



        // check if user has played the specific match before

        Optional<UserGame> existingUserGame = userGameRepository.findByUserIdAndMatchId(userId, match.getId());
        if (existingUserGame.isPresent()){
            throw new UserAlreadyPlayedException(userId, match.getId());
        }

        // create new user game if it is the first time playing this match.
        UserGame userGame = new UserGame();

        userGame.setUser(user);
        userGame.setPlayedAt(LocalDateTime.now());
        userGame.setMatch(match);
        userGame.setPredictedHomeScore(userGuess.getPredictedHomeScore());
        userGame.setPredictedAwayScore(userGuess.getPredictedAwayScore());

        //calculate other user game results
        calculateGameResult(userGame,match);

        System.out.println("user guess is " + userGuess);
        return null;
        //userGameRepository.save(userGuess);
    }

    private void calculateGameResult(UserGame userGame, Match match){
        
    }

    public User getUserStats(Long userId){

        Optional<User> user = userRepository.findById(userId);
        return user.orElse(null);
    }



}
