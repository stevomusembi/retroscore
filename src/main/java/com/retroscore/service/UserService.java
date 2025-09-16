package com.retroscore.service;


import com.retroscore.dto.GoogleUserInfo;
import com.retroscore.dto.UserSettingsDto;
import com.retroscore.entity.User;
import com.retroscore.enums.GameDifficulty;
import com.retroscore.enums.TimerDurations;
import com.retroscore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Transactional
@Service
public class UserService {
    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

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

    public void updateTimeLimit(Long userId, String timeLimit){
        User user = findById(userId);
        if(user == null){
            throw new EntityNotFoundException("user not found");
        }
        user.setTimeLimit(TimerDurations.valueOf(timeLimit.toUpperCase()));
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


    /**
     * Finds existing user by Google ID or creates a new one from Google user info
     * This method handles both web OAuth2 and mobile token validation flows
     * @param googleUserInfo User information from Google
     * @return User entity (existing or newly created)
     */
    public User findOrCreateFromGoogleInfo(GoogleUserInfo googleUserInfo) {
        logger.info("Processing Google user: {} ({})", googleUserInfo.getName(), googleUserInfo.getEmail());

        // First, try to find user by Google ID
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleUserInfo.getSub());
        if (existingUserByGoogleId.isPresent()) {
            User user = existingUserByGoogleId.get();

            // Update user info if it has changed
            updateUserFromGoogleInfo(user, googleUserInfo);

            logger.info("Found existing user by Google ID: {}", user.getEmail());
            return userRepository.save(user);
        }

        // If not found by Google ID, try to find by email (user might have registered before)
        Optional<User> existingUserByEmail = userRepository.findByEmail(googleUserInfo.getEmail());
        if (existingUserByEmail.isPresent()) {
            User user = existingUserByEmail.get();

            // Link the Google account to existing user
            user.setGoogleId(googleUserInfo.getSub());
            updateUserFromGoogleInfo(user, googleUserInfo);

            logger.info("Linked Google account to existing user: {}", user.getEmail());
            return userRepository.save(user);
        }

        // Create new user from Google info
        User newUser = createUserFromGoogleInfo(googleUserInfo);
        User savedUser = userRepository.save(newUser);

        logger.info("Created new user from Google info: {}", savedUser.getEmail());
        return savedUser;
    }

    /**
     * Creates a new User entity from Google user information
     * @param googleUserInfo Google user data
     * @return new User entity (not yet saved)
     */
    private User createUserFromGoogleInfo(GoogleUserInfo googleUserInfo) {
        User user = new User();

        // Set Google-specific fields
        user.setGoogleId(googleUserInfo.getSub());
        user.setEmail(googleUserInfo.getEmail());
        user.setEmailVerified(googleUserInfo.isEmailVerified());

        // Set name fields
        user.setName(googleUserInfo.getName());
        user.setFirstName(googleUserInfo.getGivenName());
        user.setLastName(googleUserInfo.getFamilyName());

        // Generate username from email or name
        String username = generateUsernameFromGoogleInfo(googleUserInfo);
        user.setUsername(username);

        // Set profile picture
        user.setProfilePicture(googleUserInfo.getPicture());

        // Set account status
        user.setActive(true);
        user.setAccountNonLocked(true);

        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // Set authentication provider
        user.setProvider("GOOGLE");

        // No password needed for OAuth2 users
//        user.setPassword(null);

        return user;
    }

    /**
     * Updates existing user with latest Google information
     * @param user Existing user entity
     * @param googleUserInfo Latest Google user data
     */
    private void updateUserFromGoogleInfo(User user, GoogleUserInfo googleUserInfo) {
        boolean updated = false;

        // Update name if changed
        if (googleUserInfo.getName() != null && !googleUserInfo.getName().equals(user.getName())) {
            user.setName(googleUserInfo.getName());
            updated = true;
        }

        // Update first name if changed
        if (googleUserInfo.getGivenName() != null && !googleUserInfo.getGivenName().equals(user.getFirstName())) {
            user.setFirstName(googleUserInfo.getGivenName());
            updated = true;
        }

        // Update last name if changed
        if (googleUserInfo.getFamilyName() != null && !googleUserInfo.getFamilyName().equals(user.getLastName())) {
            user.setLastName(googleUserInfo.getFamilyName());
            updated = true;
        }

        // Update profile picture if changed
        if (googleUserInfo.getPicture() != null && !googleUserInfo.getPicture().equals(user.getProfilePicture())) {
            user.setProfilePicture(googleUserInfo.getPicture());
            updated = true;
        }

        // Update email verification status
        if (googleUserInfo.isEmailVerified() && !user.isEmailVerified()) {
            user.setEmailVerified(true);
            updated = true;
        }

        // Update last login timestamp
        user.setUpdatedAt(LocalDateTime.now());
        updated = true;

        if (updated) {
            logger.info("Updated user information from Google for: {}", user.getEmail());
        }
    }

    /**
     * Generates a unique username from Google user information
     * @param googleUserInfo Google user data
     * @return unique username
     */
    private String generateUsernameFromGoogleInfo(GoogleUserInfo googleUserInfo) {
        // Try to use the part before @ in email
        String baseUsername = googleUserInfo.getEmail().split("@")[0];

        // Clean up the username (remove special characters, make lowercase)
        baseUsername = baseUsername.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        // If username is too short, use name
        if (baseUsername.length() < 3 && googleUserInfo.getGivenName() != null) {
            baseUsername = googleUserInfo.getGivenName().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
        }

        // Ensure username is unique
        String username = baseUsername;
        int counter = 1;

        while (userRepository.findByUsername(username).isPresent()) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }

    /**
     * Find user by email (used by JWT authentication)
     * @param email User email
     * @return User entity
     * @throws UsernameNotFoundException if user not found
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
