package com.career.dto;

import com.career.model.SiteBackground;

import java.time.LocalDateTime;

public record SiteBackgroundDto(
        String pageKey,
        String pageName,
        String imageUrl,
        LocalDateTime updatedAt
) {
    public static SiteBackgroundDto from(SiteBackground background) {
        return new SiteBackgroundDto(
                background.getPageKey(),
                background.getPageName(),
                background.getImageUrl(),
                background.getUpdatedAt()
        );
    }
}
