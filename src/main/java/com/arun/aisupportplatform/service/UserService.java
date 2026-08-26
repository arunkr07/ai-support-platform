package com.arun.aisupportplatform.service;

import com.arun.aisupportplatform.dto.LoginRequest;
import com.arun.aisupportplatform.dto.UserResponse;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.entity.UserRole;
import com.arun.aisupportplatform.repository.UserRepository;
import com.arun.aisupportplatform.security.JwtService;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    public UserResponse registerUser(User user){

        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already exists");
        }

        user.setRole(UserRole.CUSTOMER);

        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        User savedUser = userRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
    }


    public String loginUser(LoginRequest request){

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                )
        );

        boolean matches = passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        );

        if(!matches){
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Wrong password"
            );
        }

        return jwtService.generateToken(
                user.getEmail()
        );
    }

    public UserResponse getCurrentUser(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
