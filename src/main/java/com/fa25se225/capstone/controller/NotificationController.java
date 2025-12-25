package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.entity.Notification;
import com.fa25se225.capstone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService service;

    @GetMapping
    public ResponseEntity<List<Notification>> list(@RequestParam(defaultValue = "false",required = false) boolean unreadOnly) {
        return ResponseEntity.ok(service.getNotifications(unreadOnly));
    }

    @PostMapping("/{id}")
    public ResponseEntity<Notification>  markRead(@PathVariable String id) {
        return ResponseEntity.ok(service.getNotification(id));
    }

    @PostMapping("/readAll")
    public ResponseEntity<?> readAll() {
        service.readAll();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/public")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createNotifyPublic(@RequestBody String message) {
        service.sentNotifyAllUser(message);
        return ResponseEntity.ok("create successfully");
    }
    @GetMapping("/public")
    public ResponseEntity<Notification> markReadPublic() {
        return ResponseEntity.ok(service.getNotificationPublicNew());
    }

    @GetMapping("/public/list")
    public ResponseEntity<List<Notification>> listPublic() {
        return ResponseEntity.ok(service.getAllNotificationsPublic());
    }

}

