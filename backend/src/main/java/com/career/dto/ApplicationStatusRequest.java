package com.career.dto;

import com.career.model.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusRequest(
        @NotNull ApplicationStatus status
) {
}
