package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.Notification;

import java.util.List;

public interface NotificationService {
    void sendNotify(String email, String type, String message);
    List<Notification> getNotifications(Boolean unreadOnly);
    Notification getNotification(String id);
    void markRead(String id);
    void readAll();
    void sentNotifyAllUser(String message);

    List<Notification> getAllNotificationsPublic();
    Notification getNotificationPublicNew();

}
