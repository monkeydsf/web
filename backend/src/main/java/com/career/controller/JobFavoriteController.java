package com.career.controller;

import com.career.dto.FavoriteJobDto;
import com.career.dto.JobFavoriteToggleResponse;
import com.career.model.Job;
import com.career.model.JobFavorite;
import com.career.model.User;
import com.career.repository.JobFavoriteRepository;
import com.career.repository.JobRepository;
import com.career.service.AuthService;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
public class JobFavoriteController {
    private final JobFavoriteRepository favoriteRepository;
    private final JobRepository jobRepository;
    private final AuthService authService;

    public JobFavoriteController(JobFavoriteRepository favoriteRepository,
                                 JobRepository jobRepository,
                                 AuthService authService) {
        this.favoriteRepository = favoriteRepository;
        this.jobRepository = jobRepository;
        this.authService = authService;
    }

    @GetMapping
    public List<FavoriteJobDto> list(@RequestHeader("X-Token") String token) {
        User user = authService.requireStudent(token);
        return favoriteRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(FavoriteJobDto::from)
                .toList();
    }

    @PostMapping("/{jobId}")
    public JobFavoriteToggleResponse toggle(@RequestHeader("X-Token") String token, @PathVariable Long jobId) {
        User user = authService.requireStudent(token);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        if (favoriteRepository.existsByUserAndJob(user, job)) {
            favoriteRepository.deleteByUserAndJob(user, job);
            return new JobFavoriteToggleResponse(false, "已取消收藏");
        }
        JobFavorite favorite = new JobFavorite();
        favorite.setUser(user);
        favorite.setJob(job);
        favoriteRepository.save(favorite);
        return new JobFavoriteToggleResponse(true, "已收藏岗位");
    }

    @DeleteMapping("/{jobId}")
    @Transactional
    public JobFavoriteToggleResponse remove(@RequestHeader("X-Token") String token, @PathVariable Long jobId) {
        User user = authService.requireStudent(token);
        Job job = jobRepository.findById(jobId).orElseThrow(() -> new IllegalArgumentException("岗位不存在"));
        favoriteRepository.deleteByUserAndJob(user, job);
        return new JobFavoriteToggleResponse(false, "已取消收藏");
    }
}
