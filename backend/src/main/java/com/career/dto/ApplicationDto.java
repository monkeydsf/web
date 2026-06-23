package com.career.dto;

import com.career.model.ApplicationStatus;
import com.career.model.JobApplication;

import java.time.LocalDateTime;

public record ApplicationDto(
        Long id,
        UserDto student,
        JobDto job,
        ApplicationStatus status,
        String coverLetter,
        LocalDateTime appliedAt,
        LocalDateTime studentReadAt,
        LocalDateTime companyReadAt,
        LocalDateTime interviewAt,
        String interviewLocation,
        String interviewNote
) {
    public static ApplicationDto from(JobApplication application) {
        return new ApplicationDto(
                application.getId(),
                UserDto.from(application.getUser()),
                JobDto.from(application.getJob()),
                application.getStatus(),
                application.getCoverLetter(),
                application.getAppliedAt(),
                application.getStudentReadAt(),
                application.getCompanyReadAt(),
                application.getInterviewAt(),
                application.getInterviewLocation(),
                application.getInterviewNote()
        );
    }
}
