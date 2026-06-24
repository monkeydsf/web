package com.career.repository;

import com.career.model.ApplicationStatus;
import com.career.model.Job;
import com.career.model.JobApplication;
import com.career.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    boolean existsByUserAndJob(User user, Job job);

    @Transactional
    void deleteByJob(Job job);

    List<JobApplication> findByUserOrderByAppliedAtDesc(User user);

    List<JobApplication> findByJob_CompanyOrderByAppliedAtDesc(String company);

    Optional<JobApplication> findByUserAndJob(User user, Job job);

    List<JobApplication> findAllByOrderByAppliedAtDesc();

    long countByStatus(ApplicationStatus status);

    long countByJob_Company(String company);

    long countByJob_CompanyAndStatus(String company, ApplicationStatus status);
}
