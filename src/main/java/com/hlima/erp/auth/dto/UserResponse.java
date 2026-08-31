package com.hlima.erp.auth.dto;

import com.hlima.erp.auth.entity.User;
import com.hlima.erp.auth.entity.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email, UserRole role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
