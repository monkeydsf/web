package com.career.controller;

import com.career.dto.ApiResponse;
import com.career.dto.JobDto;
import com.career.dto.JobRequest;
import com.career.model.Job;
import com.career.model.JobStatus;
import com.career.model.Role;
import com.career.repository.ApplicationMessageRepository;
import com.career.repository.JobApplicationRepository;
import com.career.repository.JobRepository;
import com.career.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    private final JobRepository jobRepository;
    private final JobApplicationRepository applicationRepository;
    private final ApplicationMessageRepository messageRepository;
    private final AuthService authService;

    public JobController(JobRepository jobRepository,
                         JobApplicationRepository applicationRepository,
                         ApplicationMessageRepository messageRepository,
                         AuthService authService) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.messageRepository = messageRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<JobDto> list(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String city,
                             @RequestParam(required = false) String jobType,
                             @RequestParam(required = false) JobStatus status) {
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        String normalizedCity = StringUtils.hasText(city) ? city.trim() : null;
        String normalizedJobType = StringUtils.hasText(jobType) ? jobType.trim() : null;
        return jobRepository.search(normalizedKeyword, normalizedCity, normalizedJobType, status).stream()
                .map(JobDto::from)
                .toList();
    }

    @GetMapping("/mine")
    public List<JobDto> myJobs(@RequestHeader("X-Token") String token) {
        var user = authService.requireEmployer(token);
        String companyName = user.getMajor();
        if (!StringUtils.hasText(companyName)) {
            companyName = user.getFullName();
        }
        return jobRepository.findByCompanyOrderByCreatedAtDesc(companyName.trim()).stream()
                .map(JobDto::from)
                .toList();
    }

    @GetMapping("/{id}")
    public JobDto detail(@PathVariable Long id) {
        return JobDto.from(findJob(id));
    }

    @PostMapping
    public JobDto create(@RequestHeader("X-Token") String token, @Valid @RequestBody JobRequest request) {
        var user = authService.requireEmployer(token);
        String companyName = resolveCompanyName(user, request);
        Job job = apply(new Job(), request);
        job.setCompany(companyName);
        return JobDto.from(jobRepository.save(job));
    }

    @PutMapping("/{id}")
    public JobDto update(@RequestHeader("X-Token") String token,
                         @PathVariable Long id,
                         @Valid @RequestBody JobRequest request) {
        var user = authService.requireEmployer(token);
        String companyName = resolveCompanyName(user, request);
        Job job = findJob(id);
        requireJobAccess(user, job);
        apply(job, request);
        job.setCompany(companyName);
        return JobDto.from(jobRepository.save(job));
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@RequestHeader("X-Token") String token, @PathVariable Long id) {
        var user = authService.requireEmployer(token);
        Job job = findJob(id);
        requireJobAccess(user, job);
        messageRepository.deleteByApplication_Job(job);
        applicationRepository.deleteByJob(job);
        jobRepository.delete(job);
        return new ApiResponse("岗位已删除");
    }

    private Job findJob(Long id) {
        return jobRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
    }

    private Job apply(Job job, JobRequest request) {
        job.setTitle(request.title().trim());
        job.setCity(request.city().trim());
        job.setJobType(request.jobType());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setEducationRequirement(request.educationRequirement());
        job.setDescription(request.description());
        job.setRequirementText(request.requirementText());
        job.setStatus(request.status() == null ? JobStatus.OPEN : request.status());
        return job;
    }

    private String resolveCompanyName(com.career.model.User user) {
        return resolveCompanyName(user, null);
    }

    private String resolveCompanyName(com.career.model.User user, JobRequest request) {
        if (user.getRole() == Role.ADMIN && request != null && StringUtils.hasText(request.company())) {
            return request.company().trim();
        }
        String companyName = user.getMajor();
        if (!StringUtils.hasText(companyName)) {
            companyName = user.getFullName();
        }
        if (!StringUtils.hasText(companyName)) {
            throw new IllegalArgumentException("企业名称不能为空");
        }
        return companyName.trim();
    }

    private void requireJobAccess(com.career.model.User user, Job job) {
        if (user.getRole() == Role.ADMIN) {
            return;
        }
        String companyName = resolveCompanyName(user);
        if (!companyName.equalsIgnoreCase(job.getCompany())) {
            throw new SecurityException("只能操作本企业发布的岗位");
        }
    }
}
