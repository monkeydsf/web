package com.career.dto;

import com.career.model.Role;
import com.career.model.User;

public record UserDto(
        Long id,
        String username,
        Role role,
        String fullName,
        String email,
        String phone,
        String major,
        Integer graduationYear
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getMajor(),
                user.getGraduationYear()
        );
    }
}
