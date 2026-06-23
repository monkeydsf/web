package com.career.repository;

import com.career.model.JobReport;
import com.career.model.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobReportRepository extends JpaRepository<JobReport, Long> {
    List<JobReport> findAllByOrderByCreatedAtDesc();

    List<JobReport> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    long countByStatus(ReportStatus status);
}
