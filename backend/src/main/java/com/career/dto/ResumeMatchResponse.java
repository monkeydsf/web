package com.career.dto;

import java.util.List;

public record ResumeMatchResponse(
        int score,
        String summary,
        List<String> strengths,
        List<String> gaps,
        List<String> suggestions
) {
}
