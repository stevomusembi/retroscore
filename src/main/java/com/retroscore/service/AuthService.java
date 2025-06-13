package com.retroscore.service;

import com.retroscore.entity.User;
import com.retroscore.exception.ConflictException;
import com.retroscore.exception.UnauthorizedException;
import com.retroscore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Transactional
@Service
public class AuthService {

    private UserRepository userRepository;

    @Autowired
    public AuthService (UserRepository userRepository){
        this.userRepository = userRepository;

    }

    public void register(User user) {
        String username = user.getUsername();
        String password = user.getPassword();

        if(userRepository.existsByUsername(username)){
            throw new ConflictException("username already exists");
        }
        if(username != null && password!= null && password.length() > 8 && !username.isEmpty()){
            userRepository.save(user);
        }
    }

    public User login(User user){
        String username = user.getUsername();
        String password = user.getPassword();

        Optional<User> optionalUser = userRepository.findByUsernameAndPassword(username, password);
        if(optionalUser.isPresent()){
            return optionalUser.get();
        } else {
            throw new UnauthorizedException("Check username and try again");
        }
    }
}
