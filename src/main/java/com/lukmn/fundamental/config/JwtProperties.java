package com.lukmn.fundamental.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties (prefix = "jwt")
public record JwtProperties(String issuer, Duration accessTtl, Duration refreshTtl) {

}
