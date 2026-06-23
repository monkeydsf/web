package com.career.dto;

import com.career.model.InterviewSchedule;
import com.career.model.InterviewStatus;

import java.time.LocalDateTime;

public record InterviewScheduleDto(
        Long id,
        Long applicationId,
        LocalDateTime scheduledAt,
        String location,
        String note,
        InterviewStatus status,
        LocalDateTime createdAt
) {
    public static InterviewScheduleDto from(InterviewSchedule schedule) {
        return new InterviewScheduleDto(
                schedule.getId(),
                schedule.getApplication().getId(),
                schedule.getScheduledAt(),
                schedule.getLocation(),
                schedule.getNote(),
                schedule.getStatus(),
                schedule.getCreatedAt()
        );
    }
}
