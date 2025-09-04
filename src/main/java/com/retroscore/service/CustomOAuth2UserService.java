package com.retroscore.service;

import com.retroscore.controller.AuthController;
import com.retroscore.entity.User;
import com.retroscore.enums.GameDifficulty;
import com.retroscore.repository.UserRepository;
import com.retroscore.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private static final Logger log = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException  {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        User user = processOAuth2User(oAuth2User);

        return  new UserPrincipal(user,oAuth2User.getAttributes());
    }

    private User processOAuth2User(OAuth2User oAuth2User){
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String profilePicture = oAuth2User.getAttribute("picture");
        Boolean emailVerified = oAuth2User.getAttribute("email_verified");

        if(googleId ==null || email == null){
            throw new OAuth2AuthenticationException("Missing required user information from Google");
        }
        Optional<User> existingUserByGoogleId = userRepository.findByGoogleId(googleId);

        if(existingUserByGoogleId.isPresent()){
            User existingUser = existingUserByGoogleId.get();

            return updateExistingUser(existingUser, oAuth2User);
        }

        //TODO if we have local users will check if user exist by email then update their details(in case had account before google oauth)

        return createNewUserFromGoogle(oAuth2User);
    }
     private User createNewUserFromGoogle(OAuth2User oAuth2User){
        String googleId = oAuth2User.getAttribute("sub");
         String email = oAuth2User.getAttribute("email");
         String name = oAuth2User.getAttribute("name");
         String profilePicture = oAuth2User.getAttribute("picture");
         Boolean emailVerified = oAuth2User.getAttribute("email_verified");

         User newUser = new User();
         newUser.setGoogleId(googleId);
         newUser.setEmail(email);
         newUser.setProfilePicture(profilePicture);
         newUser.setEmailVerified(Boolean.TRUE.equals(emailVerified));
         newUser.setUsername(generateUniqueUsername(name));
         newUser.setCreatedAt(LocalDateTime.now());
         newUser.setLastLogin(LocalDateTime.now());
         newUser.setGamesPlayed(0);
         newUser.setGamesWon(0);
         newUser.setGamesLost(0);
         newUser.setGamesDrawn(0);
         newUser.setTotalPoints(0);
         newUser.setExactScorePredictions(0);
         newUser.setCorrectResultPredictions(0);
         newUser.setGameDifficulty(GameDifficulty.MEDIUM);
         newUser.setPreferredLeague("ALL");
         newUser.setTimeLimit(5);
         newUser.setShowHints(true);
         newUser.setNotificationsEnabled(true);
         newUser.setMatchReminders(true);
         newUser.setScoreUpdates(true);

         log.info("New user created via Google OAuth: {}", email);


         return userRepository.save(newUser);
     }

     private User updateExistingUser(User existingUser, OAuth2User oAuth2User){
        String name = oAuth2User.getAttribute("name");
         String profilePicture = oAuth2User.getAttribute("picture");
         Boolean emailVerified = oAuth2User.getAttribute("email_verified");

         if(profilePicture != null && !profilePicture.equals(existingUser.getProfilePicture())){
             existingUser.setProfilePicture(profilePicture);
         }

         if(emailVerified != null){
             existingUser.setEmailVerified(emailVerified);
         }

         existingUser.setLastLogin(LocalDateTime.now());


         log.info("User logged in via Google OAuth: {}", emailVerified);
         return userRepository.save(existingUser);

     }

     //TODO : win future add ability fo the user to enter their own username but we check for uniqueness
     private String generateUniqueUsername(String displayName){
        if(displayName == null || displayName.trim().isEmpty()){
            displayName = "RetroScoreUser";
        }
        // remove special characters and convert to lowercase
         String baseUsername = displayName.trim()
                 .toLowerCase()
                 .replaceAll("[^a-zA-Z0-9]", "")
                 .substring(0, Math.min(displayName.length(), 20)); // Limit length

         if (baseUsername.isEmpty()) {
             baseUsername = "user";
         }

         String username = baseUsername;
         int counter = 1;

         // Keep trying until we find a unique username
         while (userRepository.existsByUsername(username)) {
             username = baseUsername + counter;
             counter++;
         }

         return username;

     }
}