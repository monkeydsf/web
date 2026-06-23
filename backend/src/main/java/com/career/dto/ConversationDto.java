package com.career.dto;

import com.career.model.JobApplication;
import com.career.model.ApplicationStatus;

import java.time.LocalDateTime;

public record ConversationDto(
        Long applicationId,
        ApplicationStatus status,
        JobDto job,
        UserDto student,
        String latestMessage,
        LocalDateTime latestMessageAt,
        boolean unread
) {
    public static ConversationDto from(JobApplication application, String latestMessage, LocalDateTime latestMessageAt, boolean unread) {
        return new ConversationDto(
                application.getId(),
                application.getStatus(),
                JobDto.from(application.getJob()),
                UserDto.from(application.getUser()),
                latestMessage,
                latestMessageAt,
                unread
        );
    }
}
