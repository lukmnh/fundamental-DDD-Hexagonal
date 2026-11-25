package com.lukmn.fundamental.application.port.out;

public interface PasswordHasher {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashPassword);
}
