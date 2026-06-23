package com.career.dto;

public record DashboardStats(
        long studentCount,
        long openJobCount,
        long applicationCount,
        long pendingCount,
        long acceptedCount,
        long unreadNotificationCount,
        long interviewCount,
        long reportCount,
        long favoriteCount
) {
}
