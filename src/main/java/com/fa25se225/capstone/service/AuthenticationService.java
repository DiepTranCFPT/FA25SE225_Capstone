package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.AuthenticationResponse;
import com.fa25se225.capstone.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    AuthenticationResponse outboundAuthenticate(String code);
    void logout(LogoutRequest request);

    String forgotPassword(ForgotPasswordRequest request);
    String verifyOtp(VerifyOtpRequest request);
    String verifyEmail(VerifyEmailRequest request);
    String resetPassword(ResetPasswordRequest request);
    String changePassword(ChangePasswordRequest request);
}
