package com.lukmn.fundamental.adapter.out.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.lukmn.fundamental.application.port.out.PasswordHasher;

@Component 
public class SpringSecurityPasswordHasher implements PasswordHasher{
    
    private final PasswordEncoder passwordEncoder;
    
    SpringSecurityPasswordHasher(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String hash(String rawPassword) {
       return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String hashPassword) {
        return passwordEncoder.matches(rawPassword, hashPassword);
    }

}
