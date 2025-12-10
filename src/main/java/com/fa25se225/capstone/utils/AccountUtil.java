package com.fa25se225.capstone.utils;

import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.UserRepository;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
public class AccountUtil {

    private final UserRepository userRepository;

    public AccountUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser() {
        String currentUserEmail = getCurrentUserEmail();
        return userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private String getCurrentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public User getAccountAdmin() {
        return userRepository.findByEmail("admin123@gmail.com")
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }
}
