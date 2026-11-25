package com.lukmn.fundamental.application.service;

import com.lukmn.fundamental.application.port.in.*;
import com.lukmn.fundamental.application.port.out.AccessTokenIssuer;
import com.lukmn.fundamental.application.port.out.PasswordHasher;
import com.lukmn.fundamental.application.port.out.RefreshTokenRepository;
import com.lukmn.fundamental.application.port.out.UserRepository;
import com.lukmn.fundamental.application.support.OpaqueTokens;
import com.lukmn.fundamental.domain.exception.BusinessException;
import com.lukmn.fundamental.domain.exception.ErrorCode;
import com.lukmn.fundamental.domain.model.RefreshToken;
import com.lukmn.fundamental.domain.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Auth use cases. The only framework import allowed here is @Transactional (a deliberate, pragmatic leak).
 * Wired as a bean in a config class (Episode 8), so this class carries no @Service annotation.
 */
public class AuthService implements RegisterUseCase, LoginUseCase, RefreshTokenUseCase, LogoutUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

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

    /**
     * Rotation: the presented token is revoked and a fresh pair is issued.
     * Reuse of an already revoked token means it was stolen or replayed: every session of that user is killed.
     * noRollbackFor keeps that mass revocation committed even though an exception is thrown afterwards.
     */
    @Override
    @Transactional(noRollbackFor = BusinessException.class)
    public AuthTokens refresh(String refreshToken) {
        if(refreshToken == null || refreshToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Instant now = clock.instant();

        RefreshToken current = refreshTokens.findByTokenHash(OpaqueTokens.sha256Hex(refreshToken))
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (current.isExpired(now)){
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (current.isRevoked() || !refreshTokens.revokeIfActive((current.id()), now)){
            log.warn("Refresh token reuse detected for user {}: revoking all sessions", current.userId());
            refreshTokens.revokeAllActiveByUserId(current.userId(), now);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        User user = users.findById(current.userId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));
        return issueTokens(user, now);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()){
           return;
        }

        Instant now = clock.instant();
        refreshTokens.findByTokenHash(OpaqueTokens.sha256Hex(rawRefreshToken))
                .ifPresent(token -> refreshTokens.revokeIfActive(token.id(), now));
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokens.revokeAllActiveByUserId(userId, clock.instant());
    }
}
