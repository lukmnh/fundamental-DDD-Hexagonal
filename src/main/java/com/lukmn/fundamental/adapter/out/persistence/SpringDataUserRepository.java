package com.lukmn.fundamental.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, Long> {
    Optional<UserJpaEntity> findByUsername(String username);
    boolean existsByUsername(String username);
}
