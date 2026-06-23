package com.career.dto;

import com.career.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 40) String username,
        @NotBlank @Size(min = 6, max = 80) String password,
        @NotBlank @Size(max = 40) String fullName,
        String email,
        String phone,
        String major,
        Integer graduationYear,
        Role role
) {
}
