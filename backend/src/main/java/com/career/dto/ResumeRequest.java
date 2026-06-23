package com.career.dto;

public record ResumeRequest(
        String expectedCity,
        String expectedPosition,
        String expectedSalary,
        String education,
        String skills,
        String projectExperience,
        String portfolioUrl
) {
}
