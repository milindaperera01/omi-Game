package com.game.user_service.dto;

public class LoginResponse {
    private String token;
    private UserView user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserView getUser() {
        return user;
    }

    public void setUser(UserView user) {
        this.user = user;
    }
}
