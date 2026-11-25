package com.lukmn.fundamental.adapter.in.web;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lukmn.fundamental.adapter.in.web.dto.LoginRequest;
import com.lukmn.fundamental.adapter.in.web.dto.RegisterRequest;
import com.lukmn.fundamental.adapter.in.web.dto.TokenResponse;
import com.lukmn.fundamental.adapter.in.web.dto.UserResponse;
import com.lukmn.fundamental.application.port.in.AuthTokens;
import com.lukmn.fundamental.application.port.in.LoginUseCase;
import com.lukmn.fundamental.application.port.in.LoginUseCase.LoginCommand;
import com.lukmn.fundamental.application.port.in.RegisterUseCase;
import com.lukmn.fundamental.application.port.in.RegisterUseCase.RegisterCommand;
import com.lukmn.fundamental.domain.model.User;

import jakarta.validation.Valid;

@RestController 
@RequestMapping ("/api/auth")
public class AuthController {
    
    private final RegisterUseCase registerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenCookie refreshTokenCookie;
    
    public AuthController(RegisterUseCase registerUseCase, LoginUseCase loginUseCase, RefreshTokenCookie refreshTokenCookie){
        this.registerUseCase = registerUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshTokenCookie = refreshTokenCookie;
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
    
    private ResponseEntity<TokenResponse> withTokens(AuthTokens token){
         return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshTokenCookie.create(token.refreshToken(), token.refreshTtl()).toString())
                .body(TokenResponse.from(token));
    }

}
