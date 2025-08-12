package com.retroscore.service;


import com.retroscore.entity.User;
import com.retroscore.repository.UserRepository;
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
}
