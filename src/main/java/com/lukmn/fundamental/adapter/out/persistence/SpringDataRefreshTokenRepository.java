package com.lukmn.fundamental.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface SpringDataRefreshTokenRepository extends JpaRepository<RefreshTokenJpaEntity, Long> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
    update RefreshTokenJpaEntity t
        set t.revokedAt = :now
    where t.id = :id
    and t.revokedAt is null
    """)
    int revokeIfActive(@Param("id") Long id, @Param("now") Instant now);

    @Modifying
    @Query("""
    update RefreshTokenJpaEntity t
        set t.revokedAt = :now
    where t.userId = :userId
    and t.revokedAt is null
    """)
    int revokeAllActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);
}
