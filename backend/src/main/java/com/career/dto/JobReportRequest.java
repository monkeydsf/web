package com.career.dto;

import jakarta.validation.constraints.NotBlank;

public record JobReportRequest(
        @NotBlank String reason,
        String detail
) {
}
