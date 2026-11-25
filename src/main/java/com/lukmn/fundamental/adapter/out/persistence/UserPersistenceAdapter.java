package com.lukmn.fundamental.adapter.out.persistence;

import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import com.lukmn.fundamental.application.port.out.UserRepository;
import com.lukmn.fundamental.domain.exception.BusinessException;
import com.lukmn.fundamental.domain.exception.ErrorCode;
import com.lukmn.fundamental.domain.model.User;


@Component 
public class UserPersistenceAdapter implements UserRepository{
    
    private static final String USERNAME_UNIQUE_CONTSRAINT = "uq_users_username";
    private final SpringDataUserRepository springDataUserRepository;
    
    UserPersistenceAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(UserJpaEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataUserRepository.findByUsername(username).map(UserJpaEntity::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return springDataUserRepository.existsByUsername(username);
    }

    @Override
    public User save(User user) {
       try {
        return springDataUserRepository.saveAndFlush(UserJpaEntity.fromDomain(user)).toDomain();
       } catch (DataIntegrityViolationException e) {
            if(isUsernameUniqueConstraintViolation(e)){
                throw new BusinessException(ErrorCode.USERNAME_ALREADY_TAKEN,
                    "username '%s' already taken".formatted(user.username())
                );
            }
            throw e;
       }
    }
    
    private static boolean isUsernameUniqueConstraintViolation(DataIntegrityViolationException e) {
        return e.getCause() instanceof org.hibernate.exception.ConstraintViolationException violation &&
                USERNAME_UNIQUE_CONTSRAINT.equalsIgnoreCase(violation.getConstraintName());
    }
}
