package com.lukmn.fundamental.adapter.in.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lukmn.fundamental.adapter.in.web.dto.CurrentUserResponse;

@RestController 
public class CurrentUserController {
    
    @GetMapping("/api/me")
    public CurrentUserResponse currentUser(@AuthenticationPrincipal Jwt jwt){
        return new CurrentUserResponse(
            Long.valueOf(jwt.getSubject()),
            jwt.getClaimAsString("username"),
            jwt.getClaimAsStringList("roles")
        );
    }

}
