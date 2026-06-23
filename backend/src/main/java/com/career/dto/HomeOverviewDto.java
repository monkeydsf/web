package com.career.dto;

import java.util.List;

public record HomeOverviewDto(
        String backgroundUrl,
        long openJobCount,
        long studentCount,
        long companyCount,
        long applicationCount,
        long favoriteCount,
        List<String> spotlightTags,
        List<JobDto> featuredJobs
) {
}
