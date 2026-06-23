package com.career.dto;

public record AuthResponse(
        String token,
        UserDto user
) {
}
