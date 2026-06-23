package com.career.dto;

import com.career.model.Job;
import com.career.model.JobStatus;

import java.time.LocalDateTime;

public record JobDto(
        Long id,
        String title,
        String company,
        String city,
        String jobType,
        Integer salaryMin,
        Integer salaryMax,
        String educationRequirement,
        String description,
        String requirementText,
        JobStatus status,
        LocalDateTime createdAt
) {
    public static JobDto from(Job job) {
        return new JobDto(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getCity(),
                job.getJobType(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getEducationRequirement(),
                job.getDescription(),
                job.getRequirementText(),
                job.getStatus(),
                job.getCreatedAt()
        );
    }
}
