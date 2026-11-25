package com.lukmn.fundamental.adapter.in.web.dto;

import com.lukmn.fundamental.application.port.in.AuthTokens;

public record TokenResponse(String accessToken, String tokenType, long expiresIn) {

    public static TokenResponse from(AuthTokens authTokens) {
        return new TokenResponse(authTokens.accessToken(), "Bearer", authTokens.accessTtl().toSeconds());
    }
}
