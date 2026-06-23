package com.career.dto;

import com.career.model.JobFavorite;

import java.time.LocalDateTime;

public record FavoriteJobDto(
        Long id,
        JobDto job,
        LocalDateTime createdAt
) {
    public static FavoriteJobDto from(JobFavorite favorite) {
        return new FavoriteJobDto(
                favorite.getId(),
                JobDto.from(favorite.getJob()),
                favorite.getCreatedAt()
        );
    }
}
