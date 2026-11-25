package com.lukmn.fundamental.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank 
    @Size (min = 4, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "only letters, digits, '.', '_' and '-' are allowed")
    String username, 
    @NotBlank 
    @Size (min = 8, max = 72)
    String password) {

}
