package com.retroscore.security;

import com.retroscore.service.CustomOAuth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;

    @Autowired
    private OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for API endpoints (we're using JWT)
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management (stateless for JWT)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure URL authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow OAuth2 endpoints (needed for web authentication flow)
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/code/**"
                        ).permitAll()

                        // Allow mobile authentication endpoints (no auth required)
                        .requestMatchers(
                                "/auth/google/mobile",
                                "/auth/validate",
                                "/auth/refresh"
                        ).permitAll()

                        // Allow public API endpoints (health checks, etc.)
                        .requestMatchers(
                                "/actuator/health",
                                "/api/public/**"
                        ).permitAll()

                        // Protected endpoints (require JWT authentication)
                        .requestMatchers(
                                "/auth/me",
                                "/auth/logout"
                        ).authenticated()

                        // All other API requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 Login
                .oauth2Login(oauth2 -> oauth2
                        // Set the login page (optional, defaults to /oauth2/authorization/{registrationId})
                        .loginPage("/oauth2/authorization/google")

                        // Configure redirect URIs (where Google sends users after auth)
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/login/oauth2/code/*")
                        )

                        // Configure user info endpoint
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )

                        // Configure success handler (generates JWT and redirects to mobile app)
                        .successHandler(oAuth2AuthenticationSuccessHandler)

                        // Configure failure handler (redirects to mobile app with error)
                        .failureHandler(oAuth2AuthenticationFailureHandler)

                )

                .build();
    }

    /**
     * CORS configuration to allow requests from your mobile app and development servers
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins (add your development/production domains)
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",     // Local development
                "https://*.expo.dev",     // Expo development
                "https://*.netlify.app",  // If you have a web version
                "https://*.vercel.app",   // If you use Vercel
                "capacitor://localhost",  // If using Capacitor
                "ionic://localhost",      // If using Ionic
                "retroscoreapp://*",      // Your custom deep link scheme
                "exp://*"                 // Expo development URLs
        ));

        // Allow specific HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Allow specific headers
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Allow credentials (important for OAuth2)
        configuration.setAllowCredentials(true);

        // Set max age for preflight requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}