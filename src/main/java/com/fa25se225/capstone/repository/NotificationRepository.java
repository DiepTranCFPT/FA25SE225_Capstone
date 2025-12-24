package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,String> {
    List<Notification> findByReceiverEmailOrderByCreatedAtDesc(String receiverEmail);

    List<Notification> findByReceiverEmailAndIsReadFalseOrderByCreatedAtDesc(String receiverEmail);

    List<Notification> findAllByReceiverEmail(String receiverEmail);
}
