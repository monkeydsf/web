package com.career.controller;

import com.career.dto.DashboardStats;
import com.career.model.ApplicationStatus;
import com.career.model.InterviewSchedule;
import com.career.model.JobStatus;
import com.career.model.Notification;
import com.career.model.Role;
import com.career.repository.JobApplicationRepository;
import com.career.repository.InterviewScheduleRepository;
import com.career.repository.JobRepository;
import com.career.repository.JobFavoriteRepository;
import com.career.repository.JobReportRepository;
import com.career.repository.NotificationRepository;
import com.career.repository.UserRepository;
import com.career.service.AuthService;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final InterviewScheduleRepository interviewRepository;
    private final NotificationRepository notificationRepository;
    private final JobReportRepository reportRepository;
    private final JobFavoriteRepository favoriteRepository;
    private final AuthService authService;

    public DashboardController(UserRepository userRepository,
                               JobRepository jobRepository,
                               JobApplicationRepository applicationRepository,
                               InterviewScheduleRepository interviewRepository,
                               NotificationRepository notificationRepository,
                               JobReportRepository reportRepository,
                               JobFavoriteRepository favoriteRepository,
                               AuthService authService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.interviewRepository = interviewRepository;
        this.notificationRepository = notificationRepository;
        this.reportRepository = reportRepository;
        this.favoriteRepository = favoriteRepository;
        this.authService = authService;
    }

    @GetMapping("/stats")
    public DashboardStats stats(@RequestHeader("X-Token") String token) {
        var user = authService.requireEmployer(token);
        if (user.getRole() == Role.ADMIN) {
            return new DashboardStats(
                    userRepository.countByRole(Role.JOB_SEEKER) + userRepository.countByRole(Role.STUDENT),
                    jobRepository.countByStatus(JobStatus.OPEN),
                    applicationRepository.count(),
                    applicationRepository.countByStatus(ApplicationStatus.PENDING),
                    applicationRepository.countByStatus(ApplicationStatus.ACCEPTED),
                    notificationRepository.countByRecipientAndReadAtIsNull(user),
                    interviewRepository.count(),
                    reportRepository.countByStatus(com.career.model.ReportStatus.OPEN),
                    favoriteRepository.count()
            );
        }

        String companyName = resolveCompanyName(user);
        return new DashboardStats(
                userRepository.countByRole(Role.JOB_SEEKER) + userRepository.countByRole(Role.STUDENT),
                jobRepository.countByCompanyAndStatus(companyName, JobStatus.OPEN),
                applicationRepository.countByJob_Company(companyName),
                applicationRepository.countByJob_CompanyAndStatus(companyName, ApplicationStatus.PENDING),
                applicationRepository.countByJob_CompanyAndStatus(companyName, ApplicationStatus.ACCEPTED),
                notificationRepository.countByRecipientAndReadAtIsNull(user),
                interviewRepository.countByApplication_Job_Company(companyName),
                reportRepository.countByStatus(com.career.model.ReportStatus.OPEN),
                favoriteRepository.count()
        );
    }

    private String resolveCompanyName(com.career.model.User user) {
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
