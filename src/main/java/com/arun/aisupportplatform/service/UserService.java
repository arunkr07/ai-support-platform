package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public User registerUser(User user){

        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        return userRepository.save(user);
    }
}
