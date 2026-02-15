package com.game.user_service.controller;

import com.game.user_service.dto.*;
import com.game.user_service.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/users/{userId}")
    public UserView getUser(@PathVariable UUID userId) {
        return userService.getUser(userId);
    }

    @GetMapping("/users/{userId}/friends")
    public FriendsView getFriends(@PathVariable UUID userId) {
        return userService.getFriends(userId);
    }

    @PostMapping("/users/{userId}/friends")
    public FriendsView addFriend(@PathVariable UUID userId,
                                 @Valid @RequestBody FriendRequest request) {
        return userService.addFriend(userId, request);
    }

    @DeleteMapping("/users/{userId}/friends/{friendEmail}")
    public FriendsView removeFriend(@PathVariable UUID userId,
                                    @PathVariable String friendEmail) {
        return userService.removeFriend(userId, friendEmail);
    }
}
