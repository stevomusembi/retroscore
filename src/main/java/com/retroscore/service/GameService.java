package com.retroscore.service;

import com.retroscore.entity.Match;
import com.retroscore.entity.User;
import com.retroscore.entity.UserGame;
import com.retroscore.repository.MatchRepository;
import com.retroscore.repository.UserGameRepository;
import com.retroscore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public Match getRandomMatch() {
        Integer randomMatchId = 9;
        Optional<Match> optionalMatch = matchRepository.findById(randomMatchId);
        return optionalMatch.orElse(null);
    }

    public UserGame  submitGuess(UserGame userGame){
        return userGameRepository.save(userGame);
    }

    public User getUserStats(Long userId){
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            return user.get();
        } else {
            return null;
        }
    }

//    public void getUserHistory(){
//
//    }

}
