package com.career.dto;

import com.career.model.JobReport;
import com.career.model.ReportStatus;

import java.time.LocalDateTime;

public record JobReportDto(
        Long id,
        JobDto job,
        UserDto reporter,
        String reason,
        String detail,
        ReportStatus status,
        LocalDateTime createdAt
) {
    public static JobReportDto from(JobReport report) {
        return new JobReportDto(
                report.getId(),
                JobDto.from(report.getJob()),
                UserDto.from(report.getReporter()),
                report.getReason(),
                report.getDetail(),
                report.getStatus(),
                report.getCreatedAt()
        );
    }
}
