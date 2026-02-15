package com.game.user_service.controller;

import com.game.user_service.auth.AuthResponse;
import com.game.user_service.auth.GoogleExchangeRequest;
import com.game.user_service.auth.RefreshTokenRequest;
import com.game.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/google/exchange")
    public AuthResponse exchangeGoogle(@Valid @RequestBody GoogleExchangeRequest request) {
        return userService.exchangeGoogleCode(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return userService.refresh(request);
    }
}
