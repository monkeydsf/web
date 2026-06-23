package com.career.controller;

import com.career.dto.ApplicationMessageDto;
import com.career.dto.ApplicationMessageRequest;
import com.career.model.ApplicationMessage;
import com.career.model.JobApplication;
import com.career.model.Role;
import com.career.model.User;
import com.career.model.NotificationType;
import com.career.repository.ApplicationMessageRepository;
import com.career.repository.JobApplicationRepository;
import com.career.service.AuthService;
import com.career.service.NotificationService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/applications/{applicationId}/messages")
public class ApplicationMessageController {
    private final ApplicationMessageRepository messageRepository;
    private final JobApplicationRepository applicationRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ApplicationMessageController(ApplicationMessageRepository messageRepository,
                                        JobApplicationRepository applicationRepository,
                                        AuthService authService,
                                        NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<ApplicationMessageDto> list(@RequestHeader("X-Token") String token,
                                            @PathVariable Long applicationId) {
        User user = authService.requireUser(token);
        JobApplication application = requireAccessibleApplication(applicationId, user);
        if (user.getRole() == Role.COMPANY || user.getRole() == Role.ADMIN) {
            application.setCompanyReadAt(LocalDateTime.now());
            applicationRepository.save(application);
        } else {
            application.setStudentReadAt(LocalDateTime.now());
            applicationRepository.save(application);
        }
        return messageRepository.findByApplicationOrderByCreatedAtAsc(application).stream()
                .map(ApplicationMessageDto::from)
                .toList();
    }

    @PostMapping
    public ApplicationMessageDto send(@RequestHeader("X-Token") String token,
                                      @PathVariable Long applicationId,
                                      @RequestBody ApplicationMessageRequest request) {
        User user = authService.requireUser(token);
        JobApplication application = requireAccessibleApplication(applicationId, user);
        if (!StringUtils.hasText(request.content())) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        String content = request.content().trim();
        if (content.length() > 2000) {
            throw new IllegalArgumentException("消息内容不能超过 2000 字");
        }

        ApplicationMessage message = new ApplicationMessage();
        message.setApplication(application);
        message.setSender(user);
        message.setContent(content);
        ApplicationMessage saved = messageRepository.save(message);
        if (user.getRole() == Role.COMPANY || user.getRole() == Role.ADMIN) {
            notificationService.notifyStudent(
                    application,
                    NotificationType.MESSAGE,
                    "收到新消息",
                    "企业回复了你的投递：" + application.getJob().getTitle()
            );
        } else {
            notificationService.notifyEmployers(
                    NotificationType.MESSAGE,
                    "收到新消息",
                    user.getFullName() + " 在 " + application.getJob().getTitle() + " 下发送了新消息",
                    application.getId(),
                    application.getJob().getId()
            );
        }
        return ApplicationMessageDto.from(saved);
    }

    private JobApplication requireAccessibleApplication(Long applicationId, User user) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        if (user.getRole() == Role.JOB_SEEKER || user.getRole() == Role.STUDENT) {
            if (!application.getUser().getId().equals(user.getId())) {
                throw new SecurityException("只能查看自己的投递沟通");
            }
            return application;
        }
        if (user.getRole() == Role.COMPANY || user.getRole() == Role.ADMIN) {
            return application;
        }
        throw new SecurityException("无权查看该沟通");
    }
}
