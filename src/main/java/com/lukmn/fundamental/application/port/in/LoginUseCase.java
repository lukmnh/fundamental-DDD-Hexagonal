package com.lukmn.fundamental.application.port.in;

public interface LoginUseCase {
    AuthTokens login(LoginCommand command);

    record LoginCommand(String username, String rawPassword){}
}
