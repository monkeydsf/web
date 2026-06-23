package com.career.dto;

import com.career.model.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record JobReportStatusRequest(
        @NotNull ReportStatus status
) {
}
