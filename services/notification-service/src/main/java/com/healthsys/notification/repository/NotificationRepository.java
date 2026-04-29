package com.healthsys.notification.repository;

import com.healthsys.notification.domain.Notification;
import com.healthsys.notification.domain.NotificationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findAllByOrderByOccurredAtDesc();

    List<Notification> findByStatusOrderByOccurredAtDesc(NotificationStatus status);
}
