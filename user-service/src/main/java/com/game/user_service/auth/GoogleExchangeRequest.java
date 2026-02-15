package com.game.user_service.auth;

import jakarta.validation.constraints.NotBlank;

public class GoogleExchangeRequest {
    @NotBlank
    private String code;

    private String codeVerifier;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }
}
