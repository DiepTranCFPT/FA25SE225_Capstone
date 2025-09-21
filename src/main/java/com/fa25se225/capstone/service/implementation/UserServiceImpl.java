package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.service.UserService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {
    KafkaTemplate<String, NotificationEvent> kafkaTemplate;


}
