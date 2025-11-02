package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.configuration.properties.JwtProperties;
import com.fa25se225.capstone.configuration.properties.OAuthProperties;
import com.fa25se225.capstone.constant.PredefinedRole;
import com.fa25se225.capstone.constant.PredefinedSystemRole;
import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.AuthenticationResponse;
import com.fa25se225.capstone.dto.response.IntrospectResponse;
import com.fa25se225.capstone.dto.response.OutboundUserResponse;
import com.fa25se225.capstone.entity.*;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.InvalidatedTokenRepository;
import com.fa25se225.capstone.repository.LoginHistoryRepository;
import com.fa25se225.capstone.repository.OtpRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.httpclient.OutboundIdentityClient;
import com.fa25se225.capstone.repository.httpclient.OutboundUserClient;
import com.fa25se225.capstone.service.AuthenticationService;
import com.fa25se225.capstone.utils.RequestContextUtil;
import com.fa25se225.capstone.utils.TimeUtils;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationServiceImpl implements AuthenticationService {
    UserRepository userRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundIdentityClient outboundIdentityClient;
    PasswordEncoder passwordEncoder;
    LoginHistoryRepository loginHistoryRepository;
    NotificationProducerService notificationProducerService;
    OtpRepository otpRepository;
    OutboundUserClient outboundUserClient;
    RedisTemplate<String, Object> redisTemplate;
    RequestContextUtil requestContextUtil;

    static int MAX_FAILED_ATTEMPTS = 5;

    JwtProperties jwtProperties;

    OAuthProperties oAuthProperties;




    @Override
    @Transactional
    public AuthenticationResponse outboundAuthenticate(String code){
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(oAuthProperties.getClientId())
                .clientSecret(oAuthProperties.getClientSecret())
                .redirectUri(oAuthProperties.getRedirectUri())
                .grantType(oAuthProperties.getGrantType())
                .build());

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());
        User user = userRepository.findByEmail(userInfo.getEmail()).orElseGet(
                () -> processNewOauthUser(userInfo));

        var token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    private User processNewOauthUser(OutboundUserResponse userInfo) {
        String temporaryPassword = UUID.randomUUID().toString();

        User newUser = createUserForFirstTimeUsingOauth2Login(userInfo, temporaryPassword);
        sendTemporaryPasswordEmail(newUser.getEmail(), newUser.getFirstName(), temporaryPassword);

        return newUser;
    }



    private User createUserForFirstTimeUsingOauth2Login(OutboundUserResponse userInfo, String temporaryPassword){
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(PredefinedSystemRole.STUDENT.name()).build());

        User user = User.builder()
                .email(userInfo.getEmail())
                .password(passwordEncoder.encode(temporaryPassword))
//                .firstName(userInfo.getGivenName())
//                .lastName(userInfo.getFamilyName())
                .roles(roles)
                .emailVerified(true)
                .build();
        System.out.println("========= " + user.getEmail());
        return userRepository.save(user);
    }

    private void sendTemporaryPasswordEmail(String email, String firstName, String password) {

        notificationProducerService.sendNotification(NotificationEvent.builder()
                .chanel("EMAIL")
                .recipients(Set.of(email))
                .templateName("TEMPORARY_PASSWORD")
                .params(Map.of(
                        "firstName", firstName != null ? firstName : email.split("@")[0],
                        "password", password
                ))
                .build());

    }

    @Override
    public IntrospectResponse introspect(IntrospectRequest request)  {
        var token = request.token();
        boolean isValid = true;

        try {
            verifyAccessToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = findUserByEmailOrThrowException(request.getEmail());

        if (!user.isAccountNonLocked()) {
            throw new AppException(ErrorCode.LOCKED_ACCOUNT);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated) {
            handleFailedLogin(user);
        }

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.UNVERIFIED_EMAIL);
        }

        resetFailedAttempts(user);

        saveLoginHistory(user, true, null);

        var token = generateToken(user);

        return AuthenticationResponse.build(token, true, user.getRoles());
    }

    private void resetFailedAttempts(User user){
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }



    private void handleFailedLogin(User user){
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        userRepository.save(user);
        if (user.getFailedLoginAttempts() >= MAX_FAILED_ATTEMPTS) {
            handleAccountLock(user);
            throw new AppException(ErrorCode.MORE_THAN_5_FAILED_PASSWORD);
        }
        throw new AppException(ErrorCode.UNAUTHENTICATED);
    };

    private void handleAccountLock(User user) {
        user.setAccountLocked(true);
        user.setLockTime(Instant.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        sendAccountLockedEmail(user);
    }

    private void sendAccountLockedEmail(User user) {

        ZoneId userZone = requestContextUtil.getZoneIdOrDefault();
        String pattern = "HH:mm:ss dd/MM/yyyy";
        String lockTime = TimeUtils.formatInstant(user.getLockTime(), userZone, pattern);
        String unlockTime = TimeUtils.formatInstant(user.getLockTime().plus(24, ChronoUnit.HOURS), userZone, pattern);


        notificationProducerService.sendNotification(NotificationEvent.builder()
                .chanel("EMAIL")
                .recipients(Set.of(user.getEmail()))
                .templateName("ACCOUNT_LOCKED")
                .params(Map.of(
                                "firstName", user.getFirstName(),
                                "email", user.getEmail(),
                                "attempts", MAX_FAILED_ATTEMPTS,
                                "lockTime", lockTime,
                                "unlockTime", unlockTime))
                        .build());

    }

    private void saveLoginHistory(User user, boolean success, String failureReason) {
        try {
            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .loginSuccess(success)
                    .failureReason(failureReason)
                    .ipAddress(requestContextUtil.getCurrentIpAddress())
                    .userAgent(requestContextUtil.getCurrentUserAgent())
                    .build();
            loginHistoryRepository.save(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void logout(LogoutRequest request) {
        var signedJWT = verifyAccessToken(request.getToken());
        JWTClaimsSet claimsSet = getClaimSetFromJwt(signedJWT);

        String jit = claimsSet.getJWTID();
        Date expiryTime = claimsSet.getExpirationTime();

        String redisKey = "logout_token" + jit;
        Duration remainingTime = Duration.between(Instant.now(), expiryTime.toInstant());
        if(!remainingTime.isNegative() && !remainingTime.isZero()){
            redisTemplate.opsForValue().set(redisKey, "logged_out", remainingTime);
        }

//        InvalidatedToken invalidatedToken =
//                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
//        invalidatedTokenRepository.save(invalidatedToken);
    }


    @Override
    public AuthenticationResponse refreshToken(RefreshRequest request)  {
        var signedJWT = verifyRefreshToken(request.getToken());
        JWTClaimsSet claimsSet = getClaimSetFromJwt(signedJWT);

        String jit = claimsSet.getJWTID();
        Date expiryTime = claimsSet.getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();
        invalidatedTokenRepository.save(invalidatedToken);

        var email = claimsSet.getSubject();
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        var token = generateToken(user);

        return AuthenticationResponse.build(token, true, user.getRoles());
    }

    private SignedJWT parseAndVerifySignature(String token){
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);

            JWSVerifier verifier = new MACVerifier(jwtProperties.getSignerKey().getBytes());
            boolean isSignatureValid = signedJWT.verify(verifier);

            if (!isSignatureValid) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            String jit = getClaimSetFromJwt(signedJWT).getJWTID();
            String redisKey = "logout_token" + jit;
            Boolean isLoggedOut = redisTemplate.hasKey(redisKey);

            if (Boolean.TRUE.equals(isLoggedOut)) {
                throw new AppException(ErrorCode.INVALID_TOKEN); // Token đã bị logout
            }


//            if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
//                throw new AppException(ErrorCode.INVALID_TOKEN);
//            }

            return signedJWT;
        } catch (JOSEException | ParseException e) {
            log.error("Invalid token format or signature", e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

    }

    private SignedJWT verifyAccessToken(String token) {
        try {
            SignedJWT signedJWT = parseAndVerifySignature(token);

            Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

            if (expiryTime.before(new Date())) {
                throw new AppException(ErrorCode.ACCESS_TOKEN_EXPIRED);
            }

            return signedJWT;
        }catch (ParseException e){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private SignedJWT verifyRefreshToken(String token) {
        try {
            SignedJWT signedJWT = parseAndVerifySignature(token);

            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
            Date refreshableUntil = new Date(issueTime.toInstant()
                    .plus(jwtProperties.getRefreshableDurationInSecond(), ChronoUnit.SECONDS).toEpochMilli());

            if (refreshableUntil.before(new Date())) {
                throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
            }

            return signedJWT;
        }catch (ParseException e){
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUsername())
                .issuer(jwtProperties.getIssuer())
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(jwtProperties.getValidDurationInSecond(), ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scp", user.getRoles().stream().map(role -> role.getName()).toList())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(jwtProperties.getSignerKey().getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }


//    private String buildScope(User user) {
//        StringJoiner stringJoiner = new StringJoiner(" ");
//
//        if (!CollectionUtils.isEmpty(user.getRoles()))
//            user.getRoles().forEach(role -> {
//                stringJoiner.add("ROLE_" + role.getName());
//                if (!CollectionUtils.isEmpty(role.getPermissions()))
//                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
//            });
//
//        return stringJoiner.toString();
//    }

    @Override
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = findUserByEmailOrThrowException(request.email());
        String otp = generateOTP();
        OtpEntity otpEntity = OtpEntity.builder()
                .email(request.email())
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();
        otpRepository.save(otpEntity);

        sendResetPasswordEmail(request.email(), otp);

        return "OTP has been sent to your email";
    }

    private void sendResetPasswordEmail(String email, String otp){
        notificationProducerService.sendNotification(NotificationEvent.builder()
                .chanel("EMAIL")
                .templateName("RESET_PASSWORD_OTP")
                .params(Map.of("otp", otp))
                .recipients(Set.of(email))
                .build());
    }

    @Override
    public String verifyOtp(VerifyOtpRequest request) {
        OtpEntity otpEntity = otpRepository.findByEmailAndOtpAndUsedFalse(request.email(), request.otp())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_OTP));

        if (otpEntity.isExpired()) throw new AppException(ErrorCode.EXPIRED_OTP);

        User user = findUserByEmailOrThrowException(request.email());
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        otpEntity.setUsed(true);
        otpRepository.save(otpEntity);


        return "Password has been reset successfully";
    }

    @Override
    public String verifyEmail(VerifyEmailRequest request) {
        var user = userRepository.findByEmailAndVerificationToken(request.email(), request.token())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_VERIFICATION_TOKEN));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return "Email verified successfully";
    }

    @Override
    public String resetPassword(ResetPasswordRequest request) {
        SignedJWT signedJWT = verifyAccessToken(request.token());
        String email = getClaimSetFromJwt(signedJWT).getSubject();

        User user = findUserByEmailOrThrowException(email);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return "Password has been reset successfully";

    }

    @Override
    public String changePassword(ChangePasswordRequest request) {
            SignedJWT signedJWT = verifyAccessToken(request.token());
        String email = getClaimSetFromJwt(signedJWT).getSubject();
            User user = findUserByEmailOrThrowException(email);

           if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
               throw new AppException(ErrorCode.INVALID_PASSWORD);
            }
           if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
               throw new AppException(ErrorCode.INVALID_NEW_PASSWORD);
           }

            user.setPassword(passwordEncoder.encode(request.newPassword()));
            userRepository.save(user);




        return "Password changed successfully";
    }

    private String generateOTP() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }

    private User findUserByEmailOrThrowException(String email){
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private JWTClaimsSet getClaimSetFromJwt(SignedJWT signedJWT) {
        try {
            return signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            log.error("Could not get Jwt claims set form JWT", e);
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }



}
