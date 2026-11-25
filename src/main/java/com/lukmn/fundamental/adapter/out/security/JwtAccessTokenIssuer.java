package com.lukmn.fundamental.adapter.out.security;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.lukmn.fundamental.application.port.out.AccessTokenIssuer;
import com.lukmn.fundamental.config.JwtProperties;
import com.lukmn.fundamental.domain.model.User;

@Component 
public class JwtAccessTokenIssuer implements AccessTokenIssuer{
    
    private final JwtEncoder jwtEncoder;
    private final String issuer;
    
    JwtAccessTokenIssuer(JwtEncoder jwtEncoder, JwtProperties jwtProperties){
        this.jwtEncoder = jwtEncoder;
        this.issuer = jwtProperties.issuer();
    }

    @Override
    public String issue(User user, Instant issuedAt, Duration ttl) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(issuer)
        .subject(String.valueOf(user.id()))
        .issuedAt(issuedAt)
        .expiresAt(issuedAt.plus(ttl))
        .id(UUID.randomUUID().toString())
        .claim("username", user.username())
        .claim("roles", List.of(user.role().name()))
        .build();
        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

}
