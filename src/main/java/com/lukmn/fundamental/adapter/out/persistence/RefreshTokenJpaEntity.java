package com.lukmn.fundamental.adapter.out.persistence;

import com.lukmn.fundamental.domain.model.RefreshToken;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens")
public class RefreshTokenJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "token_hash", unique = true, length = 64, nullable = false)
    private String tokenHash;
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected RefreshTokenJpaEntity(){}

    static RefreshTokenJpaEntity fromDomain(RefreshToken token){
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.id = token.id();
        entity.userId = token.userId();
        entity.tokenHash = token.tokenHash();
        entity.expiresAt = token.expiresAt();
        entity.revokedAt = token.revokedAt();

        return entity;
    }

    RefreshToken toDomain(){
        return new RefreshToken(id, userId, tokenHash, expiresAt, revokedAt);
    }
}
