package com.career.dto;

public record CompanyProfileSeedRequest(
        String companyName,
        String website,
        String industry,
        String size,
        String address,
        String description,
        Boolean verified
) {
}
