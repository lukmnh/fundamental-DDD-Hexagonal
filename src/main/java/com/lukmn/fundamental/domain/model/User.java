package com.lukmn.fundamental.domain.model;

import java.util.Locale;
import java.util.Objects;

public record User(Long id, String username, String passwordHash, Role role) {
    public User {
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(passwordHash, "passwordHash must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }

    public static User register(String username, String passwordHash){
        return new User(null, normalizeUsername(username), passwordHash, Role.USER);
    }

    public static String normalizeUsername(String username){
        return username.trim().toLowerCase(Locale.ROOT);
    }

    @Override
    public String toString() {
        return "User[id=%s, username=%s, role=%s]".formatted(id, username, role);
    }
}
