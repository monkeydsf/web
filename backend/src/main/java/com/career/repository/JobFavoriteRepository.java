package com.career.repository;

import com.career.model.Job;
import com.career.model.JobFavorite;
import com.career.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobFavoriteRepository extends JpaRepository<JobFavorite, Long> {
    boolean existsByUserAndJob(User user, Job job);

    Optional<JobFavorite> findByUserAndJob(User user, Job job);

    List<JobFavorite> findByUserOrderByCreatedAtDesc(User user);

    @Transactional
    void deleteByUserAndJob(User user, Job job);
}
