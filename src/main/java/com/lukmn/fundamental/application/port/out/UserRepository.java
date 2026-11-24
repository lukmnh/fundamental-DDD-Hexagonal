package com.lukmn.fundamental.application.port.out;

import com.lukmn.fundamental.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    User save(User user);
}
