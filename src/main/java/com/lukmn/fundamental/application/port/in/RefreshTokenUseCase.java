package com.lukmn.fundamental.application.port.in;

public interface RefreshTokenUseCase {

    AuthTokens refresh(String refreshToken);
}
