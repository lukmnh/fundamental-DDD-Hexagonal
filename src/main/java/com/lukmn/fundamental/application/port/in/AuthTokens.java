package com.lukmn.fundamental.application.port.in;

import java.time.Duration;

public record AuthTokens(String accessToken, Duration accessTtl, String refreshToken, Duration refreshTtl) {
}
