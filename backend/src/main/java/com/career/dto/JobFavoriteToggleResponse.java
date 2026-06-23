package com.career.dto;

public record JobFavoriteToggleResponse(
        boolean favorited,
        String message
) {
}
