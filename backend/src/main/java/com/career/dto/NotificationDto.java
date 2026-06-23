package com.career.dto;

import com.career.model.Notification;
import com.career.model.NotificationType;

import java.time.LocalDateTime;

public record NotificationDto(
        Long id,
        NotificationType type,
        String title,
        String content,
        Long relatedApplicationId,
        Long relatedJobId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getRelatedApplicationId(),
                notification.getRelatedJobId(),
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
