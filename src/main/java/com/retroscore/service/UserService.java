package com.retroscore.service;


import com.retroscore.dto.UserSettingsDto;
import com.retroscore.entity.User;
import com.retroscore.enums.GameDifficulty;
import com.retroscore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findById(Long userId){
        Optional<User>  user = userRepository.findById(userId);

        return user.orElse(null);
    }

    public User updateUser(User user){
        return userRepository.save(user);
    }

    public UserSettingsDto getUserSettings(Long userId){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("User not found");
        }

        return new UserSettingsDto(
                user.isNotificationsEnabled(),
                user.isMatchReminders(),
                user.isScoreUpdates(),
                user.getPreferredLeague(),
                user.isShowHints(),
                user.getGameDifficulty(),
                user.getTimeLimit()
        );


    }

    public UserSettingsDto updateUserSettings(Long userId, UserSettingsDto settings){
            User user = findById(userId);
            if(user == null){
                throw new EntityNotFoundException("User not found");
            }
            user.setNotificationsEnabled(settings.isNotificationsEnabled());
            user.setMatchReminders(settings.isMatchReminders());
            user.setScoreUpdates(settings.isScoreUpdates());
            user.setPreferredLeague(settings.getPreferredLeague());
            user.setGameDifficulty(settings.getGameDifficulty());
            user.setShowHints(settings.isShowHints());
            user.setTimeLimit(settings.getTimeLimit());

            User updatedUser = updateUser(user);

            return new UserSettingsDto(
                    updatedUser.isNotificationsEnabled(),
                    updatedUser.isScoreUpdates(),
                    updatedUser.isMatchReminders(),
                    updatedUser.getPreferredLeague(),
                    updatedUser.isShowHints(),
                    updatedUser.getGameDifficulty(),
                    updatedUser.getTimeLimit()
            );

    }

    public void updateGameDifficulty(Long userId, String difficulty){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }
        user.setGameDifficulty(GameDifficulty.valueOf(difficulty.toUpperCase()));
        updateUser(user);

    }

    public void updateNotification(Long userId, Boolean enabled){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }
        user.setNotificationsEnabled(enabled);
        updateUser(user);

    }

    public void updatePreferredLeague(Long userId, String leagueId){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }
        user.setPreferredLeague(leagueId);
        updateUser(user);

    }
    public void updateHints(Long userId, Boolean enabled){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }
        user.setShowHints(enabled);
        updateUser(user);

    }
}
