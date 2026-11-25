package com.lukmn.fundamental.application.port.in;

public interface LogoutUseCase {
    void logout(String rawRefreshToken);
    void logoutAll(Long userId);
}
