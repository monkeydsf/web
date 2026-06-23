package com.career.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactMessageRequest(
        @NotBlank String name,
        @Email String email,
        @NotBlank String subject,
        @NotBlank String message
) {
}
