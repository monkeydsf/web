package com.career.repository;

import com.career.model.InterviewSchedule;
import com.career.model.JobApplication;
import com.career.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InterviewScheduleRepository extends JpaRepository<InterviewSchedule, Long> {
    List<InterviewSchedule> findByApplicationOrderByScheduledAtAsc(JobApplication application);

    List<InterviewSchedule> findByApplication_UserOrderByScheduledAtAsc(User user);

    List<InterviewSchedule> findAllByOrderByScheduledAtAsc();
}
