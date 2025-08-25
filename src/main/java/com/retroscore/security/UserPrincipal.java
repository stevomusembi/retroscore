package com.retroscore.security;

import com.retroscore.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class UserPrincipal implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public UserPrincipal(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }


    @Override
    public String getName() {
        return user.getGoogleId();
    }


    public User getUser() {
        return user;
    }


    public Long getUserId() {
        return user.getId();
    }


    public String getEmail() {
        return user.getEmail();
    }


    public String getUsername() {
        return user.getUsername();
    }

    public String getProfilePicture() {
        return user.getProfilePicture();
    }
}