package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.kafka.NotificationEvent;
import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.AuthenticationResponse;
import com.fa25se225.capstone.dto.response.IntrospectResponse;
import com.fa25se225.capstone.entity.InvalidatedToken;
import com.fa25se225.capstone.entity.LoginHistory;
import com.fa25se225.capstone.entity.OtpEntity;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.exception.AppException;
import com.fa25se225.capstone.exception.ErrorCode;
import com.fa25se225.capstone.repository.InvalidatedTokenRepository;
import com.fa25se225.capstone.repository.LoginHistoryRepository;
import com.fa25se225.capstone.repository.OtpRepository;
import com.fa25se225.capstone.repository.UserRepository;
import com.fa25se225.capstone.repository.httpclient.OutboundIdentityClient;
import com.fa25se225.capstone.service.AuthenticationService;
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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
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

    private static final int MAX_FAILED_ATTEMPTS = 5;

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${jwt.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${jwt.issuer}")
    protected String issuer;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    protected long REFRESHABLE_DURATION;

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";


    public AuthenticationResponse outboundAuthenticate(String code){
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        log.info("TOKEN RESPONSE {}", response);

        return AuthenticationResponse.builder()
                .token(response.getAccessToken())
                .build();
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
        user.setLockTime(LocalDateTime.now());
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        sendAccountLockedEmail(user);
    }

    private void sendAccountLockedEmail(User user) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

            notificationProducerService.sendNotification(NotificationEvent.builder()
                    .chanel("EMAIL")
                    .recipients(Set.of(user.getEmail()))
                    .templateName("ACCOUNT_LOCKED")
                    .params(Map.of(
                                    "firstName", user.getFirstName(),
                                    "email", user.getEmail(),
                                    "attempts", MAX_FAILED_ATTEMPTS,
                                    "lockTime", user.getLockTime().format(formatter),
                                    "unlockTime", user.getLockTime().plusHours(24).format(formatter)))
                            .build());

    }

    private void saveLoginHistory(User user, boolean success, String failureReason) {
        try {
            LoginHistory history = LoginHistory.builder()
                    .user(user)
                    .loginTime(LocalDateTime.now())
                    .loginSuccess(success)
                    .failureReason(failureReason)
                    .ipAddress(getCurrentIpAddress())
                    .userAgent(getCurrentUserAgent())
                    .build();
            loginHistoryRepository.save(history);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCurrentIpAddress() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest().getRemoteAddr();
        } catch (Exception e) {
            return "Unknown";
        }
    }

    private String getCurrentUserAgent() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest().getHeader("User-Agent");
        } catch (Exception e) {
            return "Unknown";
        }
    }

    @Override
    public void logout(LogoutRequest request) {
        var signedJWT = verifyAccessToken(request.getToken());
        JWTClaimsSet claimsSet = getClaimSetFromJwt(signedJWT);

        String jit = claimsSet.getJWTID();
        Date expiryTime = claimsSet.getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedTokenRepository.save(invalidatedToken);
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

            JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
            boolean isSignatureValid = signedJWT.verify(verifier);

            if (!isSignatureValid) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

            if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
                throw new AppException(ErrorCode.INVALID_TOKEN);
            }

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
                    .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli());

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
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scp", user.getRoles().stream().map(role -> role.getName()).toList())
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
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

        notificationProducerService.sendNotification(NotificationEvent.builder()
                        .chanel("EMAIL")
                        .templateName("RESET_PASSWORD_OTP")
                        .params(Map.of("otp", otp))
                        .recipients(Set.of(request.email()))
                .build());


        return "OTP has been sent to your email";
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
