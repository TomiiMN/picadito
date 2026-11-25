package com.tomasmendez.picadito.user.dto;

import java.time.Instant;

import com.tomasmendez.picadito.user.Role;
import com.tomasmendez.picadito.user.User;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        Role role,
        Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getPhone(), user.getRole(), user.getCreatedAt());
    }
}
