package com.career.dto;

import com.career.model.ContactMessage;

import java.time.LocalDateTime;

public record ContactMessageDto(
        Long id,
        String name,
        String email,
        String subject,
        String message,
        LocalDateTime createdAt
) {
    public static ContactMessageDto from(ContactMessage message) {
        return new ContactMessageDto(
                message.getId(),
                message.getName(),
                message.getEmail(),
                message.getSubject(),
                message.getMessage(),
                message.getCreatedAt()
        );
    }
}
