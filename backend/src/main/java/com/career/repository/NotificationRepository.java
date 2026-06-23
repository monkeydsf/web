package com.career.repository;

import com.career.model.Notification;
import com.career.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadAtIsNullOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndReadAtIsNull(User recipient);
}
