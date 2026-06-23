package com.career.controller;

import com.career.dto.AiPromptRequest;
import com.career.dto.AiResumeReviewResponse;
import com.career.dto.ResumeMatchResponse;
import com.career.model.Job;
import com.career.model.Resume;
import com.career.repository.JobRepository;
import com.career.repository.ResumeRepository;
import com.career.service.AuthService;
import com.career.service.ResumeAiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AuthService authService;
    private final ResumeAiService resumeAiService;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;

    public AiController(AuthService authService,
                        ResumeAiService resumeAiService,
                        ResumeRepository resumeRepository,
                        JobRepository jobRepository) {
        this.authService = authService;
        this.resumeAiService = resumeAiService;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
    }

    @PostMapping(value = "/resume-review", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AiResumeReviewResponse reviewResume(@RequestHeader("X-Token") String token,
                                               @RequestParam("file") MultipartFile file,
                                               @RequestParam(value = "prompt", required = false) String prompt) {
        authService.requireStudent(token);
        return resumeAiService.review(file, prompt);
    }

    @PostMapping("/tools/{toolKey}")
    public AiResumeReviewResponse runTool(@PathVariable String toolKey,
                                          @RequestBody(required = false) AiPromptRequest request) {
        return resumeAiService.generateTool(toolKey, request == null ? null : request.prompt(), null);
    }

    @PostMapping("/prompt-test")
    public AiResumeReviewResponse promptTest(@RequestBody(required = false) AiPromptRequest request) {
        return resumeAiService.promptOnly(request == null ? null : request.prompt());
    }

    @PostMapping("/resume-match/{jobId}")
    public ResumeMatchResponse resumeMatch(@RequestHeader("X-Token") String token,
                                           @PathVariable Long jobId) {
        var user = authService.requireStudent(token);
        Resume resume = resumeRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("请先完善简历"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        return resumeAiService.matchResumeToJob(resume, job);
    }
}
