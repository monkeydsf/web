package com.career.controller;

import com.career.dto.JobReportDto;
import com.career.dto.JobReportRequest;
import com.career.dto.JobReportStatusRequest;
import com.career.model.Job;
import com.career.model.JobReport;
import com.career.model.User;
import com.career.repository.JobRepository;
import com.career.repository.JobReportRepository;
import com.career.service.AuthService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/reports")
public class JobReportController {
    private final JobReportRepository reportRepository;
    private final JobRepository jobRepository;
    private final AuthService authService;

    public JobReportController(JobReportRepository reportRepository,
                               JobRepository jobRepository,
                               AuthService authService) {
        this.reportRepository = reportRepository;
        this.jobRepository = jobRepository;
        this.authService = authService;
    }

    @PostMapping("/jobs/{jobId}")
    public JobReportDto submit(@RequestHeader("X-Token") String token,
                               @PathVariable Long jobId,
                               @Valid @RequestBody JobReportRequest request) {
        User user = authService.requireUser(token);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        JobReport report = new JobReport();
        report.setJob(job);
        report.setReporter(user);
        report.setReason(request.reason());
        report.setDetail(request.detail());
        return JobReportDto.from(reportRepository.save(report));
    }

    @GetMapping
    public List<JobReportDto> all(@RequestHeader("X-Token") String token) {
        authService.requireEmployer(token);
        return reportRepository.findAllByOrderByCreatedAtDesc().stream().map(JobReportDto::from).toList();
    }

    @PutMapping("/{id}/status")
    public JobReportDto updateStatus(@RequestHeader("X-Token") String token,
                                     @PathVariable Long id,
                                     @Valid @RequestBody JobReportStatusRequest request) {
        authService.requireEmployer(token);
        JobReport report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("举报不存在"));
        report.setStatus(request.status());
        return JobReportDto.from(reportRepository.save(report));
    }
}
