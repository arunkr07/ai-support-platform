package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.LoginRequest;
import com.arun.aisupportplatform.dto.RegisterRequest;
import com.arun.aisupportplatform.dto.UserResponse;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.security.JwtService;
import com.arun.aisupportplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import com.arun.aisupportplatform.dto.UserResponse;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final JwtService jwtService;


    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request){

        return userService.loginUser(request);
    }

    @GetMapping("/test")
    public String test(
            @RequestParam String token
    ) {
        return jwtService.extractEmail(token);
    }

    @GetMapping("/profile")
    public String profile() {
        return "You are authenticated!";
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {

        String email = authentication.getName();

        return userService.getCurrentUser(email);
    }
}
