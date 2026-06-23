package com.career.dto;

import com.career.model.Resume;

import java.time.LocalDateTime;

public record ResumeDto(
        Long id,
        String expectedCity,
        String expectedPosition,
        String expectedSalary,
        String education,
        String skills,
        String projectExperience,
        String portfolioUrl,
        String fileName,
        String fileUrl,
        Integer matchScore,
        String matchSummary,
        LocalDateTime updatedAt
) {
    public static ResumeDto from(Resume resume) {
        return new ResumeDto(
                resume.getId(),
                resume.getExpectedCity(),
                resume.getExpectedPosition(),
                resume.getExpectedSalary(),
                resume.getEducation(),
                resume.getSkills(),
                resume.getProjectExperience(),
                resume.getPortfolioUrl(),
                resume.getFileName(),
                resume.getFileUrl(),
                null,
                null,
                resume.getUpdatedAt()
        );
    }
}
