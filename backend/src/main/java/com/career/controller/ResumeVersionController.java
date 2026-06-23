package com.career.controller;

import com.career.dto.ResumeVersionDto;
import com.career.repository.ResumeRepository;
import com.career.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/resume/versions")
public class ResumeVersionController {
    private final ResumeRepository resumeRepository;
    private final AuthService authService;

    public ResumeVersionController(ResumeRepository resumeRepository, AuthService authService) {
        this.resumeRepository = resumeRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<ResumeVersionDto> list(@RequestHeader("X-Token") String token) {
        var user = authService.requireStudent(token);
        return resumeRepository.findByUser(user)
                .map(resume -> List.of(ResumeVersionDto.from(resume)))
                .orElseGet(List::of);
    }
}
