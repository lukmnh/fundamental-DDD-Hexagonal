package com.lukmn.fundamental.application.service;

import com.lukmn.fundamental.application.port.in.AuthTokens;
import com.lukmn.fundamental.application.port.in.LoginUseCase;
import com.lukmn.fundamental.application.port.in.RegisterUseCase;
import com.lukmn.fundamental.application.port.out.AccessTokenIssuer;
import com.lukmn.fundamental.application.port.out.PasswordHasher;
import com.lukmn.fundamental.application.port.out.RefreshTokenRepository;
import com.lukmn.fundamental.application.port.out.UserRepository;
import com.lukmn.fundamental.application.support.OpaqueTokens;
import com.lukmn.fundamental.domain.exception.BusinessException;
import com.lukmn.fundamental.domain.exception.ErrorCode;
import com.lukmn.fundamental.domain.model.RefreshToken;
import com.lukmn.fundamental.domain.model.User;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Auth use cases. The only framework import allowed here is @Transactional (a deliberate, pragmatic leak).
 * Wired as a bean in a config class (Episode 8), so this class carries no @Service annotation.
 */
public class AuthService implements RegisterUseCase, LoginUseCase {

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordHasher passwordHasher;
    private final AccessTokenIssuer accessTokenIssuer;
    private final Clock clock;
    private final TokenTtl tokenTtl;

    public AuthService(UserRepository users,
                       RefreshTokenRepository refreshTokens,
                       PasswordHasher passwordHasher,
                       AccessTokenIssuer accessTokenIssuer,
                       Clock clock,
                       TokenTtl tokenTtl){
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordHasher = passwordHasher;
        this.accessTokenIssuer = accessTokenIssuer;
        this.clock = clock;
        this.tokenTtl = tokenTtl;
    }

    @Override
    @Transactional
    public AuthTokens login(LoginCommand command) {
        Optional<User> found = users.findByUsername(User.normalizeUsername(command.username()));
        if(found.isEmpty()){
            // Burn a hash anyway so "unknown user" takes as long as "wrong password"
            passwordHasher.hash(command.rawPassword());
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        User user = found.get();
        if(!passwordHasher.matches(command.rawPassword(), user.passwordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(user, clock.instant());
    }

    @Override
    @Transactional
    public User register(RegisterCommand command) {
        String username = User.normalizeUsername(command.username());
        if(users.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.USERNAME_ALREADY_TAKEN,
                    "Username '%s' is already taken".formatted(username));
        }
        return users.save(User.register(username, passwordHasher.hash(command.rawPassword())));
    }

    private AuthTokens issueTokens(User user, Instant now){
        String accessToken = accessTokenIssuer.issue(user, now, tokenTtl.access());
        String rawRefreshToken = OpaqueTokens.generate();

        refreshTokens.save(RefreshToken.issue(
                user.id(),
                OpaqueTokens.sha256Hex(rawRefreshToken),
                now.plus(tokenTtl.refresh())
        ));
        return new AuthTokens(accessToken, tokenTtl.access(), rawRefreshToken, tokenTtl.refresh());
    }
}
