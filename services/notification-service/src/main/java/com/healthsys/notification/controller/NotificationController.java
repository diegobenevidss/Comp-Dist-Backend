package com.healthsys.notification.controller;

import com.healthsys.notification.dto.NotificationResponse;
import com.healthsys.notification.service.NotificationService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public List<NotificationResponse> list(@RequestParam(required = false) Boolean unread) {
        return Boolean.TRUE.equals(unread)
            ? notificationService.listUnread()
            : notificationService.listAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public ResponseEntity<NotificationResponse> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(notificationService.getById(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('ADMIN', 'RECEPTIONIST', 'HEALTH_PROFESSIONAL')")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(notificationService.markAsRead(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
