package com.career.dto;

import com.career.model.CompanyProfile;

import java.time.LocalDateTime;

public record CompanyProfileDto(
        String companyName,
        String website,
        String industry,
        String size,
        String address,
        String description,
        boolean verified,
        LocalDateTime updatedAt
) {
    public static CompanyProfileDto from(CompanyProfile profile) {
        return new CompanyProfileDto(
                profile.getCompanyName(),
                profile.getWebsite(),
                profile.getIndustry(),
                profile.getSize(),
                profile.getAddress(),
                profile.getDescription(),
                profile.isVerified(),
                profile.getUpdatedAt()
        );
    }
}
