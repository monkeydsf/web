package com.career.dto;

import com.career.model.JobStatus;
import jakarta.validation.constraints.NotBlank;

public record JobRequest(
        @NotBlank String title,
        @NotBlank String company,
        @NotBlank String city,
        String jobType,
        Integer salaryMin,
        Integer salaryMax,
        String educationRequirement,
        String description,
        String requirementText,
        JobStatus status
) {
}
