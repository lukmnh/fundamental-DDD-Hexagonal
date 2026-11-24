package com.lukmn.fundamental.adapter.out.persistence;

import com.lukmn.fundamental.domain.model.Role;
import com.lukmn.fundamental.domain.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    protected UserJpaEntity(){}

    static UserJpaEntity fromDomain(User user){
        UserJpaEntity entity = new UserJpaEntity();
        entity.id = user.id();
        entity.username = user.username();
        entity.passwordHash = user.passwordHash();
        entity.role = user.role();
        return entity;
    }

    User toDomain(){
        return new User(id, username, passwordHash, role);
    }
}
