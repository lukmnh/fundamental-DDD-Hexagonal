package com.lukmn.fundamental.application.service;

import java.time.Duration;
import java.util.Objects;

public record TokenTtl(Duration access, Duration refresh) {
    public TokenTtl{
        Objects.requireNonNull(access, "access cannot be null");
        Objects.requireNonNull(refresh, "refresh cannot be null");
    }
}
