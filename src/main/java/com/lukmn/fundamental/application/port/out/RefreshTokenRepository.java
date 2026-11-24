package com.lukmn.fundamental.application.port.out;

import com.lukmn.fundamental.domain.model.RefreshToken;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    /**
     * Atomic compare-and-set revoke: only succeeds if the token is still active.
     *
     * @return true if this call revoked the token, false if it was already revoked
     */
    boolean revokeIfActive(Long tokenId, Instant now);
    void revokeAllActiveByUserId(Long userId, Instant now);
}
