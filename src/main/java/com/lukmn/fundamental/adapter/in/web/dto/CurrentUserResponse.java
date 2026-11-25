package com.lukmn.fundamental.adapter.in.web.dto;

import java.util.List;

public record CurrentUserResponse(Long id, String username, List<String> roles) {

}
