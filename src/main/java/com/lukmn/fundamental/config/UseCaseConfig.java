package com.lukmn.fundamental.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.lukmn.fundamental.application.port.out.AccessTokenIssuer;
import com.lukmn.fundamental.application.port.out.PasswordHasher;
import com.lukmn.fundamental.application.port.out.RefreshTokenRepository;
import com.lukmn.fundamental.application.port.out.UserRepository;
import com.lukmn.fundamental.application.service.AuthService;
import com.lukmn.fundamental.application.service.TokenTtl;

@Configuration 
public class UseCaseConfig {
    
    @Bean 
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public AuthService authService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordHasher passwordHasher,
        AccessTokenIssuer accessTokenIssuer, Clock clock, JwtProperties jwtProperties
    ){
        return new AuthService(userRepository, refreshTokenRepository, passwordHasher, accessTokenIssuer, clock, new TokenTtl(jwtProperties.accessTtl(), jwtProperties.refreshTtl()));
    }

}
