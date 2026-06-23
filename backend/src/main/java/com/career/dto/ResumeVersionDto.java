package com.career.dto;

import com.career.model.Resume;

import java.time.LocalDateTime;

public record ResumeVersionDto(
        Long id,
        String fileName,
        String fileUrl,
        String parsedText,
        LocalDateTime updatedAt
) {
    public static ResumeVersionDto from(Resume resume) {
        return new ResumeVersionDto(
                resume.getId(),
                resume.getFileName(),
                resume.getFileUrl(),
                resume.getParsedText(),
                resume.getUpdatedAt()
        );
    }
}
