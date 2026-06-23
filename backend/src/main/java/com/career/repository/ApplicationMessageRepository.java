package com.career.repository;

import com.career.model.ApplicationMessage;
import com.career.model.Job;
import com.career.model.JobApplication;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationMessageRepository extends JpaRepository<ApplicationMessage, Long> {
    List<ApplicationMessage> findByApplicationOrderByCreatedAtAsc(JobApplication application);

    Optional<ApplicationMessage> findTopByApplicationOrderByCreatedAtDesc(JobApplication application);

    @Transactional
    void deleteByApplication(JobApplication application);

    @Transactional
    void deleteByApplication_Job(Job job);
}
