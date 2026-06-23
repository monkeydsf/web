package com.career.controller;

import com.career.dto.HomeOverviewDto;
import com.career.dto.JobDto;
import com.career.model.JobStatus;
import com.career.model.Role;
import com.career.repository.JobApplicationRepository;
import com.career.repository.JobFavoriteRepository;
import com.career.repository.JobRepository;
import com.career.repository.SiteBackgroundRepository;
import com.career.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/home")
public class HomeController {
    private final JobRepository jobRepository;
    private final SiteBackgroundRepository backgroundRepository;
    private final UserRepository userRepository;
    private final JobApplicationRepository applicationRepository;
    private final JobFavoriteRepository favoriteRepository;

    public HomeController(JobRepository jobRepository,
                         SiteBackgroundRepository backgroundRepository,
                         UserRepository userRepository,
                         JobApplicationRepository applicationRepository,
                         JobFavoriteRepository favoriteRepository) {
        this.jobRepository = jobRepository;
        this.backgroundRepository = backgroundRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.favoriteRepository = favoriteRepository;
    }

    @GetMapping
    public HomeOverviewDto overview() {
        String backgroundUrl = backgroundRepository.findById("home")
                .map(background -> background.getImageUrl())
                .orElse("/backgrounds/home.svg");
        List<JobDto> featuredJobs = jobRepository.search(null, null, null, JobStatus.OPEN).stream()
                .limit(6)
                .map(JobDto::from)
                .toList();
        return new HomeOverviewDto(
                backgroundUrl,
                jobRepository.countByStatus(JobStatus.OPEN),
                userRepository.countByRole(Role.JOB_SEEKER) + userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.COMPANY),
                applicationRepository.count(),
                favoriteRepository.count(),
                List.of("Java 后端", "前端开发", "实习", "校招", "杭州", "上海"),
                featuredJobs
        );
    }
}
