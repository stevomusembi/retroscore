package com.retroscore.security;

import com.retroscore.service.CustomOAuth2UserService;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Autowired
    private OptionalJWTAuthenticationFilter optionalJwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Disable CSRF for API endpoints (we're using JWT)
                .csrf(csrf -> csrf.disable())

                // Configure CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Configure session management (stateless for JWT, but allow sessions for web OAuth2)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                // Configure URL authorization
                .authorizeHttpRequests(auth -> auth
                        // Allow OAuth2 endpoints (needed for WEB authentication flow)
                        .requestMatchers(
                                "/oauth2/**",
                                "/login/oauth2/code/**",
                                "/login**",
                                "/error**"
                        ).permitAll()

                        // Allow MOBILE authentication endpoints (no auth required)
                        .requestMatchers(
                                "/auth/google/mobile",
                                "/auth/validate",
                                "/auth/refresh",
                                "/auth/guest"
                        ).permitAll()

                        // Allow public API endpoints (health checks, etc.)
                        .requestMatchers(
                                "/actuator/health",
                                "/api/public/**"
                        ).permitAll()

                        // Mixed access endpoints (authenticated + anonymous)
                        .requestMatchers(
                                "/api/game/**",           // Game endpoints
                                "/api/matches/**",        // Browse matches
                                "/api/leaderboard/public" // Public leaderboard
                        ).permitAll()  // Allow both authenticated and anonymous

                        // WEB-only protected endpoints (require OAuth2 session OR JWT)
                        .requestMatchers(
                                "/dashboard/**",          // Web dashboard
                                "/profile/**"             // Web profile pages
                        ).authenticated()

                        // API-only protected endpoints (require JWT)
                        .requestMatchers(
                                "/api/user/**",           // User profile API
                                "/api/game/save-progress", // Save game progress
                                "/api/leaderboard/personal", // Personal stats
                                "/auth/me",
                                "/auth/logout"
                        ).authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 Login (for WEB users)
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

                        // Configure success handler (for WEB - creates session/JWT and redirects)
                        .successHandler(oAuth2AuthenticationSuccessHandler)

                        // Configure failure handler (for WEB - redirects with error)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // Add JWT filter for API requests (doesn't interfere with OAuth2 sessions)
                .addFilterBefore((Filter) optionalJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow specific origins
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",         // Local development
                "https://yourdomain.com",     // Your web app domain
                "https://*.expo.dev",         // Expo development
                "https://*.netlify.app",      // If you have a web version
                "https://*.vercel.app",       // If you use Vercel
                "capacitor://localhost",      // If using Capacitor
                "ionic://localhost",          // If using Ionic
                "retroscoreapp://*",          // Your custom deep link scheme
                "exp://*"                     // Expo development URLs
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization", "Content-Type", "X-Requested-With",
                "Accept", "Origin", "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}