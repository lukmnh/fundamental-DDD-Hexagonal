package com.lukmn.fundamental.adapter.in.web;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component 
public class RefreshTokenCookie {
    
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String PATH = "/api/auth";
    
    public ResponseCookie create(String rawToken, Duration maxAge){
        return base(rawToken)
        .maxAge(maxAge)
        .build();
    }
    
    public ResponseCookie clear(){
        return base("")
        .maxAge(Duration.ZERO)
        .build();
    }
    
    private ResponseCookie.ResponseCookieBuilder base(String value){
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path(PATH);
    }

}
