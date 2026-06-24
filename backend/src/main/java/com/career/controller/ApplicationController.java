package com.career.controller;

import com.career.dto.ApiResponse;
import com.career.dto.ApplicationDto;
import com.career.dto.ApplicationRequest;
import com.career.dto.ApplicationStatusRequest;
import com.career.model.Job;
import com.career.model.JobApplication;
import com.career.model.JobStatus;
import com.career.model.NotificationType;
import com.career.model.Role;
import com.career.model.User;
import com.career.repository.JobApplicationRepository;
import com.career.repository.JobRepository;
import com.career.repository.ApplicationMessageRepository;
import com.career.service.AuthService;
import com.career.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final JobApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ApplicationMessageRepository messageRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public ApplicationController(JobApplicationRepository applicationRepository,
                                 JobRepository jobRepository,
                                 ApplicationMessageRepository messageRepository,
                                 AuthService authService,
                                 NotificationService notificationService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.messageRepository = messageRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @PostMapping("/jobs/{jobId}")
    public ApplicationDto apply(@RequestHeader("X-Token") String token,
                                @PathVariable Long jobId,
                                @RequestBody ApplicationRequest request) {
        User user = authService.requireStudent(token);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        if (job.getStatus() != JobStatus.OPEN) {
            throw new IllegalArgumentException("该岗位已停止招聘");
        }
        if (applicationRepository.existsByUserAndJob(user, job)) {
            throw new IllegalArgumentException("你已投递过该岗位");
        }
        JobApplication application = new JobApplication();
        application.setUser(user);
        application.setJob(job);
        application.setCoverLetter(request.coverLetter());
        JobApplication saved = applicationRepository.save(application);
        notificationService.notifyEmployers(
                NotificationType.APPLICATION,
                "收到新投递",
                user.getFullName() + " 投递了 " + job.getTitle(),
                saved.getId(),
                job.getId()
        );
        return ApplicationDto.from(saved);
    }

    @GetMapping("/mine")
    public List<ApplicationDto> mine(@RequestHeader("X-Token") String token) {
        User user = authService.requireStudent(token);
        return applicationRepository.findByUserOrderByAppliedAtDesc(user).stream()
                .map(ApplicationDto::from)
                .toList();
    }

    @GetMapping("/by-job/{jobId}")
    public ApplicationDto byJob(@RequestHeader("X-Token") String token, @PathVariable Long jobId) {
        User user = authService.requireStudent(token);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        JobApplication application = applicationRepository.findByUserAndJob(user, job)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        return ApplicationDto.from(application);
    }

    @GetMapping("/{id}")
    public ApplicationDto detail(@RequestHeader("X-Token") String token, @PathVariable Long id) {
        User user = authService.requireUser(token);
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        if ((user.getRole() == Role.JOB_SEEKER || user.getRole() == Role.STUDENT)
                && !application.getUser().getId().equals(user.getId())) {
            throw new SecurityException("只能查看自己的投递");
        }
        if (user.getRole() == Role.COMPANY) {
            requireCompanyApplicationAccess(user, application);
        }
        if (user.getRole() != Role.JOB_SEEKER
                && user.getRole() != Role.STUDENT
                && user.getRole() != Role.COMPANY
                && user.getRole() != Role.ADMIN) {
            throw new SecurityException("无权查看该投递");
        }
        return ApplicationDto.from(application);
    }

    @GetMapping
    public List<ApplicationDto> all(@RequestHeader("X-Token") String token) {
        User user = authService.requireEmployer(token);
        if (user.getRole() != Role.ADMIN) {
            return companyApplications(user);
        }
        return applicationRepository.findAllByOrderByAppliedAtDesc().stream()
                .map(ApplicationDto::from)
                .toList();
    }

    @GetMapping("/company")
    public List<ApplicationDto> companyApps(@RequestHeader("X-Token") String token) {
        User user = authService.requireEmployer(token);
        return companyApplications(user);
    }

    private List<ApplicationDto> companyApplications(User user) {
        String companyName = resolveCompanyName(user);
        return applicationRepository.findByJob_CompanyOrderByAppliedAtDesc(companyName).stream()
                .map(ApplicationDto::from)
                .toList();
    }

    @PutMapping("/{id}/status")
    public ApplicationDto changeStatus(@RequestHeader("X-Token") String token,
                                       @PathVariable Long id,
                                       @Valid @RequestBody ApplicationStatusRequest request) {
        User user = authService.requireEmployer(token);
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        requireCompanyApplicationAccess(user, application);
        application.setStatus(request.status());
        JobApplication saved = applicationRepository.save(application);
        notificationService.notifyStudent(
                saved,
                NotificationType.APPLICATION,
                "投递状态更新",
                "你的 " + saved.getJob().getTitle() + " 投递状态已更新为 " + saved.getStatus()
        );
        return ApplicationDto.from(saved);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse cancel(@RequestHeader("X-Token") String token, @PathVariable Long id) {
        User user = authService.requireStudent(token);
        JobApplication application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        if (!application.getUser().getId().equals(user.getId())) {
            throw new SecurityException("只能撤回自己的投递");
        }
        messageRepository.deleteByApplication(application);
        applicationRepository.delete(application);
        return new ApiResponse("投递已撤回");
    }

    private void requireCompanyApplicationAccess(User user, JobApplication application) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        String companyName = resolveCompanyName(user);
        if (!companyName.equalsIgnoreCase(application.getJob().getCompany())) {
            throw new SecurityException("只能操作本企业岗位的投递");
        }
    }

    private String resolveCompanyName(User user) {
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
