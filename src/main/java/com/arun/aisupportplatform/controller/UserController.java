package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.LoginRequest;
import com.arun.aisupportplatform.dto.RegisterRequest;
import com.arun.aisupportplatform.dto.UserResponse;
import com.arun.aisupportplatform.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody LoginRequest request){

        return userService.loginUser(request);
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
