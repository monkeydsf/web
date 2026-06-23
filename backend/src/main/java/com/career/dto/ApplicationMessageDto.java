package com.career.dto;

import com.career.model.ApplicationMessage;

import java.time.LocalDateTime;

public record ApplicationMessageDto(
        Long id,
        Long applicationId,
        UserDto sender,
        String content,
        LocalDateTime createdAt
) {
    public static ApplicationMessageDto from(ApplicationMessage message) {
        return new ApplicationMessageDto(
                message.getId(),
                message.getApplication().getId(),
                UserDto.from(message.getSender()),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
