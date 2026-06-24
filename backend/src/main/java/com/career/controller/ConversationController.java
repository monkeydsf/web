package com.career.controller;

import com.career.dto.ConversationDto;
import com.career.model.JobApplication;
import com.career.model.Role;
import com.career.repository.ApplicationMessageRepository;
import com.career.repository.JobApplicationRepository;
import com.career.service.AuthService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {
    private final JobApplicationRepository applicationRepository;
    private final ApplicationMessageRepository messageRepository;
    private final AuthService authService;

    public ConversationController(JobApplicationRepository applicationRepository,
                                  ApplicationMessageRepository messageRepository,
                                  AuthService authService) {
        this.applicationRepository = applicationRepository;
        this.messageRepository = messageRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<ConversationDto> list(@RequestHeader("X-Token") String token) {
        var user = authService.requireUser(token);
        List<JobApplication> applications;
        if (user.getRole() == Role.ADMIN) {
            applications = applicationRepository.findAllByOrderByAppliedAtDesc();
        } else if (user.getRole() == Role.COMPANY) {
            applications = applicationRepository.findByJob_CompanyOrderByAppliedAtDesc(companyName(user));
        } else {
            applications = applicationRepository.findByUserOrderByAppliedAtDesc(user);
        }
        return applications.stream()
                .map(application -> {
                    var latest = messageRepository.findTopByApplicationOrderByCreatedAtDesc(application).orElse(null);
                    String latestMessage = latest == null ? "" : latest.getContent();
                    var latestAt = latest == null ? application.getAppliedAt() : latest.getCreatedAt();
                    boolean unread = user.getRole() == Role.COMPANY || user.getRole() == Role.ADMIN
                            ? application.getCompanyReadAt() == null || (latest != null && application.getCompanyReadAt().isBefore(latestAt))
                            : application.getStudentReadAt() == null || (latest != null && application.getStudentReadAt().isBefore(latestAt));
                    return ConversationDto.from(application, latestMessage, latestAt, unread);
                })
                .toList();
    }

    private String companyName(com.career.model.User user) {
        String companyName = user.getMajor();
        if (!StringUtils.hasText(companyName)) {
            companyName = user.getFullName();
        }
        if (!StringUtils.hasText(companyName)) {
            throw new IllegalArgumentException("企业名称不能为空");
        }
        return companyName.trim();
    }
}
