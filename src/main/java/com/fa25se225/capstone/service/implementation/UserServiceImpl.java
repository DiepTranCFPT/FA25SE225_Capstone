package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedRole;
import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.UserCreationRequest;
import com.fa25se225.capstone.dto.request.UserRoleUpdateRequest;
import com.fa25se225.capstone.dto.request.UserUpdateRequest;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.UserMapper;
import com.fa25se225.capstone.repository.RoleRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.UserService;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    PageHelper pageHelper;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    NotificationProducerService notificationProducerService;

    @Override
    public UserResponse register(UserCreationRequest request) {

        if(userRepository.existsByEmail(request.email())){
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }

        String verificationToken = UUID.randomUUID().toString();
        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setVerificationToken(verificationToken);
        var savedUser = userRepository.save(user);

        notificationProducerService.sendNotification(NotificationEvent.builder()
                .chanel("EMAIL")
                .recipients(Set.of(request.email()))
                .templateName("VERIFY_EMAIL")
                .params(Map.of(
                        "firstName", request.firstName(),
                        "verificationLink", "http://your-frontend-url/verify-email?email=" + request.email() + "&token=" + verificationToken
                ))
                .build());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getMyProfile() {
        String email = getCurrentEmail();
        return userMapper.toResponse(findUserByEmail(email));
    }

    @Override
    public PageResponse<List<UserResponse>> getAllUserSortBy(int pageNo, int pageSize, String... sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);
        Page<User> page = userRepository.findAll(pageable);
        List<UserResponse> userResponses = page.getContent()
                .stream().map(userMapper::toResponse).toList();

        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(userResponses.size())
                .sortBy(sorts)
                .items(userResponses)
                .build();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = findUserByEmail(getCurrentEmail());
        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.password()));
        return userMapper.toResponse(userRepository.save(user));
    }

    //
    @Override
    public UserResponse updateUserRole(String id, UserRoleUpdateRequest request) {
        User user = findUserId(id);
        List<Role> roles = roleRepository.findAllById(request.roles());

        user.setRoles(new HashSet<>(roles));
        userRepository.save(user);
        return null;
    }

    @Override
    public void delete(String userId) {
        User user = findUserId(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    public User findUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    public User findUserId(String id){
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }


    public String getCurrentEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }




}
