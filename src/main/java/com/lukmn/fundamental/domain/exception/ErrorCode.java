package com.lukmn.fundamental.domain.exception;

public enum ErrorCode {
    USERNAME_ALREADY_TAKEN("AUTH-001", "Username is already taken"),
    INVALID_CREDENTIALS("AUTH-002", "Invalid username or password"),
    INVALID_REFRESH_TOKEN("AUTH-003", "Refresh token is missing, invalid, expired or revoked");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
