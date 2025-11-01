package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.configuration.properties.JwtProperties;
import com.fa25se225.capstone.dto.request.PermissionRequest;
import com.fa25se225.capstone.dto.response.PermissionResponse;
import com.fa25se225.capstone.entity.Permission;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.mapper.PermissionMapper;
import com.fa25se225.capstone.repository.PermissionRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.service.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PermissionServiceImpl implements PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    UserRepository userRepository;
    RedisTemplate<String, Object> redisTemplate;
    JwtProperties jwtProperties;

    static  String CACHE_KEY_PREFIX = "user_permissions::";


    @Override
    public PermissionResponse create(PermissionRequest request) {
        Permission permission = permissionMapper.toPermission(request);
        permission = permissionRepository.save(permission);
        return permissionMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream().map(permissionMapper::toPermissionResponse).toList();
    }

    @Override
    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public Collection<GrantedAuthority> getAuthoritiesForUser(String userEmail) {
        String cacheKey = CACHE_KEY_PREFIX + userEmail;

        Set<String> cachedPermissions = (Set<String>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedPermissions != null) {
            log.debug("Cache hit for user: {}", userEmail);
            return cachedPermissions.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());
        }

        log.debug("Cache miss for user: {}. Calculating from DB.", userEmail);
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found for authority calculation: " + userEmail));

        Set<String> effectivePermissions = calculateEffectivePermissions(user);

        redisTemplate.opsForValue().set(cacheKey, effectivePermissions, jwtProperties.getValidDurationInSecond(), TimeUnit.SECONDS);

        return effectivePermissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());
    }

    public Set<String> calculateEffectivePermissions(User user) {
        Set<String> permissions = new HashSet<>();

        user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .forEach(permission -> permissions.add(permission.getName()));

        user.getGrantedPermissions()
                .forEach(permission -> permissions.add(permission.getName()));

        user.getRevokedPermissions()
                .forEach(permission -> permissions.remove(permission.getName()));

        return permissions;
    }

    @Override
    public void clearUserPermissionsCache(String userEmail) {
        String cacheKey = CACHE_KEY_PREFIX + userEmail;
        log.info("Clearing permission cache for user: {}", userEmail);
        redisTemplate.delete(cacheKey);
    }

    @Override
    public List<PermissionResponse> getPermissionByUserId(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Set<String> permissionNames = calculateEffectivePermissions(user);
        return permissionRepository.findAllById(permissionNames).stream().map(permissionMapper::toPermissionResponse).toList();
    }
}

