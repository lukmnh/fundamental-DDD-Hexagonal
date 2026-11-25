package com.lukmn.fundamental.adapter.in.web.dto;

import com.lukmn.fundamental.domain.model.User;

public record UserResponse(Long id, String username, String role) {
    public static UserResponse from(User user){
        return new UserResponse(user.id(), user.username(), user.role().name());
    }

}
