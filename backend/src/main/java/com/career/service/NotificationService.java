package com.career.service;

import com.career.model.JobApplication;
import com.career.model.JobReport;
import com.career.model.Notification;
import com.career.model.NotificationType;
import com.career.model.Role;
import com.career.model.User;
import com.career.repository.NotificationRepository;
import com.career.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public Notification create(User recipient,
                               NotificationType type,
                               String title,
                               String content,
                               Long applicationId,
                               Long jobId) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setRelatedApplicationId(applicationId);
        notification.setRelatedJobId(jobId);
        return notificationRepository.save(notification);
    }

    public void notifyEmployers(NotificationType type, String title, String content, Long applicationId, Long jobId) {
        List<User> recipients = userRepository.findAllByRoleIn(List.of(Role.COMPANY, Role.ADMIN));
        recipients.forEach(user -> create(user, type, title, content, applicationId, jobId));
    }

    public void notifyStudent(JobApplication application, NotificationType type, String title, String content) {
        create(application.getUser(), type, title, content, application.getId(), application.getJob().getId());
    }

    public void notifyReporter(User reporter, NotificationType type, String title, String content, Long jobId) {
        create(reporter, type, title, content, null, jobId);
    }

    public void notifyReporters(JobReport report, String title, String content) {
        notifyEmployers(NotificationType.REPORT, title, content, null, report.getJob().getId());
    }

    public void markRead(Notification notification) {
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
