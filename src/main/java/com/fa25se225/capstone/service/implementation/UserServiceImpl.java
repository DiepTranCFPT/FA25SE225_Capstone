package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.UserMapper;
import com.fa25se225.capstone.repository.*;
import com.fa25se225.capstone.repository.specs.UserSpecification;
import com.fa25se225.capstone.service.PermissionService;
import com.fa25se225.capstone.service.TeacherProfileService;
import com.fa25se225.capstone.service.UserService;
import com.fa25se225.capstone.utils.AccountUtil;
import com.fa25se225.capstone.utils.PageHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;


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
    CloudinaryService cloudinaryService;
    static String AVATAR_FOLDER = "user_avatars";
    PermissionService permissionService;
    PermissionRepository permissionRepository;
    TeacherProfileService teacherProfileService;

    StudentProfileRepository studentProfileRepository;
    ParentProfileRepository parentProfileRepository;
    TeacherProfileRepository teacherProfileRepository;


    @Override
    @Transactional
    public User createUser(UserCreationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }

        String verificationToken = UUID.randomUUID().toString();

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setVerificationToken(verificationToken);

        Role role = roleRepository.findById(request.roleName())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ROLE_NAME));

        user.setRoles(Set.of(role));

        User savedUser = userRepository.save(user);


        notificationProducerService.sendNotification(NotificationEvent.builder()
                .chanel("EMAIL")
                .recipients(Set.of(request.email()))
                .templateName("VERIFY_EMAIL")
                .params(Map.of(
                        "firstName", request.firstName(),
                        "verificationLink", "https://capstoneproject-production-857f.up.railway.app/verify-email/?email=" + request.email() + "&token=" + verificationToken
                ))
                .build());

        return savedUser;
    }


    @Override
    public UserResponse getMyProfile() {
        String email = getCurrentEmail();
        User user = findUserByEmailOrThrowException(email);
        StudentProfile student = studentProfileRepository.findByUserId(user.getId()).orElse(null);
        ParentProfile parent = parentProfileRepository.findByUserId(user.getId()).orElse(null);
        TeacherProfile teacher = teacherProfileRepository.findByUserId(user.getId()).orElse(null);
        return userMapper.toResponse(user, teacher, parent, student);

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
                .totalElement(page.getTotalElements())
                .sortBy(sorts)
                .items(userResponses)
                .build();
    }

    @Override
    public UserResponse update(UserUpdateRequest request) {
        User user = findUserByEmailOrThrowException(getCurrentEmail());
        userMapper.updateUser(user, request);
        return userMapper.toResponse(userRepository.save(user));
    }

    //
    @Override
    public UserResponse updateUserRole(String id, UserRoleUpdateRequest request) {
        User user = findUserIdOrThrowException(id);
        List<Role> roles = roleRepository.findAllById(request.roles());

        user.setRoles(new HashSet<>(roles));
        return userMapper.toResponse(userRepository.save(user));

    }

    @Override
    public void delete(String userId) {
        User user = findUserIdOrThrowException(userId);
        user.setDeleted(true);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponse updateUserAvatar(MultipartFile file) {
        User user = findUserByEmailOrThrowException(getCurrentEmail());
        if(Strings.isNotEmpty(user.getImgUrl())){
            String publicId = cloudinaryService.getPublicIdFromUrl(user.getImgUrl());
            if (Objects.nonNull(publicId)) {
                cloudinaryService.deleteFile(publicId);
            }
        }
        String newAvatarUrl = cloudinaryService.uploadFile(file, AVATAR_FOLDER);
        user.setImgUrl(newAvatarUrl);
        userRepository.save(user);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse deleteUserAvatar() {
        User user = findUserByEmailOrThrowException(getCurrentEmail());
        String avatarUrl = user.getImgUrl();
        if(Strings.isEmpty(avatarUrl)){
            return userMapper.toResponse(user);
        }
        String publicId = cloudinaryService.getPublicIdFromUrl(avatarUrl);
        if(Strings.isNotEmpty(publicId)){
            cloudinaryService.deleteFile(publicId);
        }

        user.setImgUrl(null);
        userRepository.save(user);
        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getUnverifiedTeachers() {
        List<User> teachers = userRepository.findUnverifiedTeachers("TEACHER");
        List<UserResponse> responses = new ArrayList<>();
        for (User teacher : teachers) {
            TeacherProfileResponse teacherProfile = teacherProfileService.getProfileByUserId(teacher.getId());
            if(teacherProfile.getIsVerified() == false){
                responses.add(userMapper.toResponse(teacher, teacherProfile));
            }
        }
        return responses;
    }

    @Override
    @Transactional
    public UserResponse verifyTeacher(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        TeacherProfile teacherProfile1 = teacherProfileRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        teacherProfile1.setIsVerified(true);
        teacherProfileRepository.save(teacherProfile1);
        TeacherProfileResponse teacherProfile = teacherProfileService.getProfileByUserId(user.getId());
        return userMapper.toResponse(user, teacherProfile);
    }

    @Override
    public UserResponse getProfileByUserId(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        TeacherProfileResponse teacherProfile = null;
        if (user.getRoles().stream().anyMatch(r -> "TEACHER".equals(r.getName()))) {
            teacherProfile = teacherProfileService.getProfileByUserId(user.getId());
        }
        return userMapper.toResponse(user, teacherProfile);
    }

    private User findUserByEmailOrThrowException(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private User findUserIdOrThrowException(String id){
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }


    public String getCurrentEmail(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }


    @Override
    @Transactional
    public void grantPermissions(String userId, Set<String> permissionNames) {
        User user = findUserIdOrThrowException(userId);
        Set<Permission> permissionsToGrant = new HashSet<>(permissionRepository.findAllById(permissionNames));

        user.getGrantedPermissions().addAll(permissionsToGrant);
        user.getRevokedPermissions().removeAll(permissionsToGrant);

        userRepository.save(user);

        permissionService.clearUserPermissionsCache(user.getEmail());
    }

    @Override
    @Transactional
    public void revokePermissions(String userId, Set<String> permissionNames) {
        User user = findUserIdOrThrowException(userId);
        Set<Permission> permissionsToRevoke = new HashSet<>(permissionRepository.findAllById(permissionNames));

        user.getRevokedPermissions().addAll(permissionsToRevoke);
        user.getGrantedPermissions().removeAll(permissionsToRevoke);

        userRepository.save(user);

        permissionService.clearUserPermissionsCache(user.getEmail());
    }

    @Override
    public PageResponse<List<UserResponse>> searchUsers(UserSearchRequest request, int pageNo, int pageSize, String... sorts) {
        Pageable pageable = pageHelper.pageEngine(pageNo, pageSize, sorts);

        Specification<User> spec = UserSpecification.getSpec(request);
        Page<User> page = userRepository.findAll(spec, pageable);

        List<UserResponse> items = page.getContent().stream()
                .map(userMapper::toResponse)
                .toList();

        return PageResponse.<List<UserResponse>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalPage(page.getTotalPages())
                .totalElement(page.getTotalElements())
                .items(items)
                .build();
    }



}
