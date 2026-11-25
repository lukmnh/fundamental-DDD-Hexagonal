package com.lukmn.fundamental.adapter.out.persistence;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.lukmn.fundamental.application.port.out.RefreshTokenRepository;
import com.lukmn.fundamental.domain.model.RefreshToken;

@Component 
public class RefreshTokenPersistenceAdapter implements RefreshTokenRepository{
    
    private final SpringDataRefreshTokenRepository springDataRefreshTokenRepository;
    
    RefreshTokenPersistenceAdapter(SpringDataRefreshTokenRepository springDataRefreshTokenRepository) {
        this.springDataRefreshTokenRepository = springDataRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return springDataRefreshTokenRepository.save(RefreshTokenJpaEntity.fromDomain(token)).toDomain();
    }

    @Override
    public Optional<RefreshToken> findByTokenHash(String tokenHash) {
        return springDataRefreshTokenRepository.findByTokenHash(tokenHash).map(RefreshTokenJpaEntity::toDomain);
    }

    @Override
    public boolean revokeIfActive(Long tokenId, Instant now) {
       return springDataRefreshTokenRepository.revokeIfActive(tokenId, now) == 1;
    }

    @Override
    public void revokeAllActiveByUserId(Long userId, Instant now) {
        springDataRefreshTokenRepository.revokeAllActiveByUserId(userId, now);
    }

}
