package com.arun.aisupportplatform.controller;

import com.arun.aisupportplatform.dto.LoginRequest;
import com.arun.aisupportplatform.dto.UserResponse;
import com.arun.aisupportplatform.entity.User;
import com.arun.aisupportplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@RequestBody User user){
        return userService.registerUser(user);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request){

        return userService.loginUser(request);
    }
}
