package com.retroscore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retroscore.dto.GoogleUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

@Service
public class GoogleTokenValidatorService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenValidatorService.class);

    // Google's token info endpoint
    private static final String GOOGLE_TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo?access_token=";

    // Alternative: Google's userinfo endpoint (more reliable for user data)
    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo?access_token=";

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String webClientId;

    // Add Android client ID as well
    @Value("${spring.security.oauth2.client.registration.google.android-client-id:}")
    private String androidClientId;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoogleTokenValidatorService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Validates Google access token and returns user information
     * @param accessToken Google access token from mobile app
     * @return GoogleUserInfo if token is valid, null if invalid
     */
    public GoogleUserInfo validateToken(String accessToken) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            logger.warn("Empty or null access token provided");
            return null;
        }

        try {
            // First, validate the token and get basic info
            if (!isTokenValid(accessToken)) {
                logger.warn("Token validation failed");
                return null;
            }

            // Then get user information
            return getUserInfo(accessToken);

        } catch (Exception e) {
            logger.error("Error validating Google token", e);
            return null;
        }
    }

    /** check audience equal to either
      *  web or android client id
     */
    private boolean isValidAudience(String audience) {
        return audience.equals(webClientId) ||
                (audience.equals(androidClientId));
    }

    /**
     * Validates token using Google's tokenInfo endpoint
     * @param accessToken Google access token
     * @return true if valid, false otherwise
     */
    private boolean isTokenValid(String accessToken) {
        try {
            String url = GOOGLE_TOKEN_INFO_URL + accessToken;
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                logger.warn("No response from Google token validation");
                return false;
            }

            // Parse the response
            JsonNode jsonResponse = objectMapper.readTree(response);

            // Check if there's an error
            if (jsonResponse.has("error")) {
                logger.warn("Google token validation error: {}", jsonResponse.get("error").asText());
                return false;
            }

            // Validate audience (client_id) - security check
            if (jsonResponse.has("audience")) {
                String audience = jsonResponse.get("audience").asText();
                if (!isValidAudience(audience)) {
                    logger.warn("Token audience mismatch. Expected: {} or {}, Got: {}", webClientId,androidClientId, audience);
                    return false;
                }
            }

            // Check token expiration
            if (jsonResponse.has("expires_in")) {
                int expiresIn = jsonResponse.get("expires_in").asInt();
                if (expiresIn <= 0) {
                    logger.warn("Google token has expired");
                    return false;
                }
            }

            logger.info("Google token validation successful");
            return true;

        } catch (RestClientException e) {
            logger.error("Network error validating Google token", e);
            return false;
        } catch (Exception e) {
            logger.error("Error parsing Google token validation response", e);
            return false;
        }
    }

    /**
     * Gets user information from Google using the access token
     * @param accessToken Valid Google access token
     * @return GoogleUserInfo with user details
     */
    private GoogleUserInfo getUserInfo(String accessToken) {
        try {
            String url = GOOGLE_USERINFO_URL + accessToken;
            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                logger.warn("No response from Google userinfo endpoint");
                return null;
            }

            // Parse user information
            JsonNode jsonResponse = objectMapper.readTree(response);

            // Check for error
            if (jsonResponse.has("error")) {
                logger.warn("Google userinfo error: {}", jsonResponse.get("error").asText());
                return null;
            }

            // Extract user information
            GoogleUserInfo userInfo = new GoogleUserInfo();
            userInfo.setSub(jsonResponse.has("id") ? jsonResponse.get("id").asText() : null);
            userInfo.setEmail(jsonResponse.has("email") ? jsonResponse.get("email").asText() : null);
            userInfo.setName(jsonResponse.has("name") ? jsonResponse.get("name").asText() : null);
            userInfo.setPicture(jsonResponse.has("picture") ? jsonResponse.get("picture").asText() : null);
            userInfo.setEmailVerified(jsonResponse.has("verified_email") && jsonResponse.get("verified_email").asBoolean());
            userInfo.setGivenName(jsonResponse.has("given_name") ? jsonResponse.get("given_name").asText() : null);
            userInfo.setFamilyName(jsonResponse.has("family_name") ? jsonResponse.get("family_name").asText() : null);

            // Validate required fields
            if (userInfo.getSub() == null || userInfo.getEmail() == null) {
                logger.warn("Missing required user information from Google");
                return null;
            }

            // Security check: ensure email is verified
            if (!userInfo.isEmailVerified()) {
                logger.warn("Google account email is not verified: {}", userInfo.getEmail());
                return null;
            }

            logger.info("Successfully retrieved user info for: {}", userInfo.getEmail());
            return userInfo;

        } catch (RestClientException e) {
            logger.error("Network error getting Google user info", e);
            return null;
        } catch (Exception e) {
            logger.error("Error parsing Google userinfo response", e);
            return null;
        }
    }

    /**
     * Alternative method: Validate ID token (if mobile apps send ID tokens instead)
     * This is more secure as ID tokens contain signed claims
     * @param idToken Google ID token (JWT)
     * @return GoogleUserInfo if valid, null if invalid
     */
    public GoogleUserInfo validateIdToken(String idToken) {
        // This would require additional JWT parsing library
        // For now, we're using access tokens which is simpler
        // but ID tokens are more secure for this use case

        logger.warn("ID token validation not implemented yet - use access token validation");
        return null;
    }

    /**
     * Refresh token validation (if needed in the future)
     * @param refreshToken Google refresh token
     * @return new access token if valid
     */
    public String refreshAccessToken(String refreshToken) {
        // Implementation for refresh token flow
        // Not needed for basic mobile authentication
        logger.warn("Refresh token functionality not implemented");
        return null;
    }
}