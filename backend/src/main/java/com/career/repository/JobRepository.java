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
                or lower(j.company) like lower(concat('%', :keyword, '%')))
              and (:city is null or j.city = :city)
              and (:status is null or j.status = :status)
            order by j.createdAt desc
            """)
    List<Job> search(@Param("keyword") String keyword,
                     @Param("city") String city,
                     @Param("status") JobStatus status);

    long countByStatus(JobStatus status);
}
