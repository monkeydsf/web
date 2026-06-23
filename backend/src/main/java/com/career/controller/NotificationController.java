package com.career.controller;

import com.career.dto.NotificationDto;
import com.career.dto.NotificationSummaryDto;
import com.career.model.Notification;
import com.career.model.User;
import com.career.repository.NotificationRepository;
import com.career.service.AuthService;
import com.career.service.NotificationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final AuthService authService;

    public NotificationController(NotificationRepository notificationRepository,
                                  NotificationService notificationService,
                                  AuthService authService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
        this.authService = authService;
    }

    @GetMapping
    public List<NotificationDto> list(@RequestHeader("X-Token") String token) {
        User user = authService.requireUser(token);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user).stream()
                .map(NotificationDto::from)
                .toList();
    }

    @GetMapping("/summary")
    public NotificationSummaryDto summary(@RequestHeader("X-Token") String token) {
        User user = authService.requireUser(token);
        return new NotificationSummaryDto(notificationRepository.countByRecipientAndReadAtIsNull(user));
    }

    @PatchMapping("/{id}/read")
    public NotificationDto read(@RequestHeader("X-Token") String token, @PathVariable Long id) {
        User user = authService.requireUser(token);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("通知不存在"));
        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new SecurityException("无权操作该通知");
        }
        notificationService.markRead(notification);
        return NotificationDto.from(notification);
    }
}
