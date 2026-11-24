package com.lukmn.fundamental.domain.model;

import java.time.Instant;
import java.util.Objects;

public record RefreshToken(Long id, Long userId, String tokenHash, Instant expiresAt, Instant revokedAt) {
    public RefreshToken{
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(tokenHash, "tokenHash cannot be null");
        Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
    }

    public static RefreshToken issue(Long userId, String tokenHash, Instant expiresAt){
        return new RefreshToken(null, userId, tokenHash, expiresAt, null);
    }

    public boolean isRevoked(){
        return revokedAt != null;
    }

    public boolean isExpired(Instant now){
        return !now.isBefore(expiresAt);
    }

}
