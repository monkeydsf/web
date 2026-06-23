package com.career.dto;

public record AiResumeReviewResponse(
        String content,
        String model,
        Integer promptTokens,
        Integer completionTokens,
        Integer totalTokens
) {
}
