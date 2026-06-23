package com.career.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record InterviewScheduleRequest(
        @NotNull LocalDateTime scheduledAt,
        String location,
        String note
) {
}
