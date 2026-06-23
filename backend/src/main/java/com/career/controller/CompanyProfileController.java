package com.career.controller;

import com.career.dto.CompanyProfileDto;
import com.career.dto.CompanyProfileRequest;
import com.career.model.CompanyProfile;
import com.career.repository.CompanyProfileRepository;
import com.career.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/company-profiles")
public class CompanyProfileController {
    private final CompanyProfileRepository repository;
    private final AuthService authService;

    public CompanyProfileController(CompanyProfileRepository repository, AuthService authService) {
        this.repository = repository;
        this.authService = authService;
    }

    @GetMapping("/{companyName}")
    public CompanyProfileDto get(@PathVariable String companyName) {
        return repository.findById(companyName)
                .map(CompanyProfileDto::from)
                .orElseThrow(() -> new IllegalArgumentException("公司主页不存在"));
    }

    @GetMapping
    public java.util.List<CompanyProfileDto> list() {
        return repository.findAll().stream().map(CompanyProfileDto::from).toList();
    }

    @PostMapping
    public CompanyProfileDto create(@RequestHeader("X-Token") String token, @Valid @RequestBody CompanyProfileRequest request) {
        authService.requireAdmin(token);
        CompanyProfile profile = new CompanyProfile();
        profile.setCompanyName(request.companyName());
        apply(profile, request);
        return CompanyProfileDto.from(repository.save(profile));
    }

    @PutMapping("/{companyName}")
    public CompanyProfileDto update(@RequestHeader("X-Token") String token,
                                    @PathVariable String companyName,
                                    @Valid @RequestBody CompanyProfileRequest request) {
        authService.requireAdmin(token);
        CompanyProfile profile = repository.findById(companyName)
                .orElseGet(CompanyProfile::new);
        profile.setCompanyName(companyName);
        apply(profile, request);
        return CompanyProfileDto.from(repository.save(profile));
    }

    private void apply(CompanyProfile profile, CompanyProfileRequest request) {
        profile.setWebsite(request.website());
        profile.setIndustry(request.industry());
        profile.setSize(request.size());
        profile.setAddress(request.address());
        profile.setDescription(request.description());
        if (request.verified() != null) {
            profile.setVerified(request.verified());
        }
    }
}
