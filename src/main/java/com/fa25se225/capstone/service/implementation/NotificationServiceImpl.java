package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.NotificationPayload;
import com.fa25se225.capstone.entity.Notification;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.NotificationRepository;
import com.fa25se225.capstone.service.NotificationService;
import com.fa25se225.capstone.service.OnlineUserService;
import com.fa25se225.capstone.utils.AccountUtil;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Service
public class NotificationServiceImpl implements NotificationService {


    private final NotificationRepository repo;
    private final SimpMessagingTemplate template;
    private final OnlineUserService onlineUserService;
    private final AccountUtil accountUtil;

    public NotificationServiceImpl(NotificationRepository repo,
                                   SimpMessagingTemplate template,
                                   OnlineUserService onlineUserService, AccountUtil accountUtil) {
        this.repo = repo;
        this.template = template;
        this.onlineUserService = onlineUserService;
        this.accountUtil = accountUtil;
    }

    @Override
    public void sendNotify(String email, String type, String message) {

        Notification e = new Notification();
        e.setReceiverEmail(email);
        e.setType(type);
        e.setMessage(message);
        e.setCreatedAt(Instant.now());
        e.setRead(false);
        repo.save(e);

        if (onlineUserService.isOnline(email)) {
            NotificationPayload payload = new NotificationPayload(e.getId(),type, e.getMessage(),e.getCreatedAt(), false);
            template.convertAndSendToUser(email, "/queue/notifications", payload);
        }
    }

    @Override
    public List<Notification> getNotifications(Boolean unreadOnly) {
        String email= accountUtil.getCurrentUser().getEmail();
        if (Objects.isNull(unreadOnly)) {
           return repo.findAllByReceiverEmail(email);
        }
        return unreadOnly
                ? repo.findByReceiverEmailAndIsReadFalseOrderByCreatedAtDesc(email)
                : repo.findByReceiverEmailOrderByCreatedAtDesc(email);
    }

    @Override
    public Notification getNotification(String id) {
        String email= accountUtil.getCurrentUser().getEmail();

        Notification e = repo.findById(id).orElseThrow(()->new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!email.equals(e.getReceiverEmail())) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        if(e.isRead()) return e;

        e.setRead(true);
        return repo.save(e);
    }

    @Override
    public void markRead(String id) {
        String email= accountUtil.getCurrentUser().getEmail();

        Notification n = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!email.equals(n.getReceiverEmail())) {
            throw new RuntimeException("Not your notification");
        }
        n.setRead(true);
        repo.save(n);
    }

    @Override
    public void readAll() {
        String email= accountUtil.getCurrentUser().getEmail();
        List<Notification> list = repo.findByReceiverEmailAndIsReadFalseOrderByCreatedAtDesc(email);
        list.forEach(n -> n.setRead(true));
        repo.saveAll(list);
    }

    @Override
    public void sentNotifyAllUser(String message) {
        String email = accountUtil.getAccountAdmin().getEmail();
        Notification e = new Notification();
        e.setReceiverEmail(email);
        e.setMessage(message);
        e.setCreatedAt(Instant.now());
        e.setType("NOTIFICATION SYSTEM");
        e.setRead(true);
        repo.save(e);
    }

    @Override
    public List<Notification> getAllNotificationsPublic() {
        return repo.findAllByReceiverEmail(accountUtil.getAccountAdmin().getEmail());
    }

    @Override
    public Notification getNotificationPublicNew() throws AppException {
        List<Notification> notifications = repo.findByReceiverEmailOrderByCreatedAtDesc(accountUtil.getAccountAdmin().getEmail());

        if (notifications.isEmpty()) {
            throw new AppException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }

        return notifications.get(0);    }

}
