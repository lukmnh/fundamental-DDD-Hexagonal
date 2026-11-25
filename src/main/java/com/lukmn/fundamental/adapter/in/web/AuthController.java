package com.lukmn.fundamental.adapter.in.web;

import com.lukmn.fundamental.application.port.in.*;
import com.lukmn.fundamental.application.port.out.RefreshTokenRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.lukmn.fundamental.adapter.in.web.dto.LoginRequest;
import com.lukmn.fundamental.adapter.in.web.dto.RegisterRequest;
import com.lukmn.fundamental.adapter.in.web.dto.TokenResponse;
import com.lukmn.fundamental.adapter.in.web.dto.UserResponse;
import com.lukmn.fundamental.application.port.in.LoginUseCase.LoginCommand;
import com.lukmn.fundamental.application.port.in.RegisterUseCase.RegisterCommand;
import com.lukmn.fundamental.domain.model.User;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/auth")
public class AuthController {
    
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenCookie refreshTokenCookie;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LogoutUseCase logoutUseCase;


    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase, RefreshTokenCookie refreshTokenCookie, RefreshTokenUseCase refreshTokenUseCase, RefreshTokenRepository refreshTokenRepository, LogoutUseCase logoutUseCase){
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.refreshTokenRepository = refreshTokenRepository;
        this.logoutUseCase = logoutUseCase;
    }
    
    @PostMapping ("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        User user = registerUseCase.register(new RegisterCommand(request.username(), request.password()));
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }
    
    @PostMapping ("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request){
        AuthTokens tokens = loginUseCase.login(new LoginCommand(request.username(), request.password()));
        return withTokens(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@CookieValue(name = RefreshTokenCookie.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken){
        return withTokens(refreshTokenUseCase.refresh(refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = RefreshTokenCookie.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken) {
        logoutUseCase.logout(refreshToken);
        return clearedCookie();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal Jwt jwt) {
        logoutUseCase.logoutAll(Long.valueOf(jwt.getSubject()));
        return clearedCookie();
    }
    
    private ResponseEntity<TokenResponse> withTokens(AuthTokens token){
         return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.create(token.refreshToken(), token.refreshTtl()).toString())
                .body(TokenResponse.from(token));
    }

    private ResponseEntity<Void> clearedCookie() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.clear().toString())
                .build();
    }

}
