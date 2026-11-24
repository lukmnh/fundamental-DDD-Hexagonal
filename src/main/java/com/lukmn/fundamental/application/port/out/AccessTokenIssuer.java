package com.lukmn.fundamental.application.port.out;

import com.lukmn.fundamental.domain.model.User;

import java.time.Duration;
import java.time.Instant;

public interface AccessTokenIssuer {
    /** Returns a signed, self-contained access token (JWT in the current adapter). */
    String issue(User user, Instant issuedAt, Duration ttl);
}
