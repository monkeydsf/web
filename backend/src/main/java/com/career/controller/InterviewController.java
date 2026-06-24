package com.career.controller;

import com.career.dto.InterviewScheduleDto;
import com.career.dto.InterviewScheduleRequest;
import com.career.model.InterviewSchedule;
import com.career.model.JobApplication;
import com.career.model.Role;
import com.career.model.User;
import com.career.repository.InterviewScheduleRepository;
import com.career.repository.JobApplicationRepository;
import com.career.service.AuthService;
import com.career.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private final InterviewScheduleRepository interviewRepository;
    private final JobApplicationRepository applicationRepository;
    private final AuthService authService;
    private final NotificationService notificationService;

    public InterviewController(InterviewScheduleRepository interviewRepository,
                               JobApplicationRepository applicationRepository,
                               AuthService authService,
                               NotificationService notificationService) {
        this.interviewRepository = interviewRepository;
        this.applicationRepository = applicationRepository;
        this.authService = authService;
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<InterviewScheduleDto> list(@RequestHeader("X-Token") String token) {
        User user = authService.requireUser(token);
        List<InterviewSchedule> schedules;
        if (user.getRole() == Role.ADMIN) {
            schedules = interviewRepository.findAllByOrderByScheduledAtAsc();
        } else if (user.getRole() == Role.COMPANY) {
            schedules = interviewRepository.findByApplication_Job_CompanyOrderByScheduledAtAsc(companyName(user));
        } else {
            schedules = interviewRepository.findByApplication_UserOrderByScheduledAtAsc(user);
        }
        return schedules.stream().map(InterviewScheduleDto::from).toList();
    }

    @PostMapping("/applications/{applicationId}")
    public InterviewScheduleDto create(@RequestHeader("X-Token") String token,
                                       @PathVariable Long applicationId,
                                       @Valid @RequestBody InterviewScheduleRequest request) {
        User user = authService.requireEmployer(token);
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("投递记录不存在"));
        if (user.getRole() == Role.COMPANY && !companyName(user).equalsIgnoreCase(application.getJob().getCompany())) {
            throw new SecurityException("只能为本企业岗位安排面试");
        }
        InterviewSchedule schedule = new InterviewSchedule();
        schedule.setApplication(application);
        schedule.setScheduledAt(request.scheduledAt());
        schedule.setLocation(request.location());
        schedule.setNote(request.note());
        InterviewSchedule saved = interviewRepository.save(schedule);

        application.setInterviewAt(request.scheduledAt());
        application.setInterviewLocation(request.location());
        application.setInterviewNote(request.note());
        applicationRepository.save(application);

        notificationService.notifyStudent(
                application,
                com.career.model.NotificationType.INTERVIEW,
                "面试安排",
                "你有一场 " + application.getJob().getTitle() + " 的面试安排"
        );
        return InterviewScheduleDto.from(saved);
    }

    private String companyName(User user) {
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
