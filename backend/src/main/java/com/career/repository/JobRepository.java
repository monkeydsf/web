package com.career.repository;

import com.career.model.Job;
import com.career.model.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByTitleAndCompany(String title, String company);

    @Query("""
            select j from Job j
            where (:keyword is null or lower(j.title) like lower(concat('%', :keyword, '%'))
                or lower(j.company) like lower(concat('%', :keyword, '%'))
                or lower(j.description) like lower(concat('%', :keyword, '%'))
                or lower(j.requirementText) like lower(concat('%', :keyword, '%'))
                or lower(j.city) like lower(concat('%', :keyword, '%')))
              and (:city is null or lower(j.city) like lower(concat('%', :city, '%')))
              and (:jobType is null or j.jobType = :jobType)
              and (:status is null or j.status = :status)
            order by j.createdAt desc
            """)
    List<Job> search(@Param("keyword") String keyword,
                     @Param("city") String city,
                     @Param("jobType") String jobType,
                     @Param("status") JobStatus status);

    List<Job> findByCompanyOrderByCreatedAtDesc(String company);

    long countByStatus(JobStatus status);
}
