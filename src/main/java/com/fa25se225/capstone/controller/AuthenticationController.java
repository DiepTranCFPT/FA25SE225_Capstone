package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.*;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.dto.response.AuthenticationResponse;
import com.fa25se225.capstone.dto.response.UserResponse;
import com.fa25se225.capstone.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(name = "Authentication", description = "APIs for user authentication, authorization, and account management")
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/outbound/authentication")
    @Operation(summary = "Outbound Authentication (Google)",
            description = "Authenticates a user using an authorization code from an external provider like Google and returns JWT tokens.")
    ApiResponse<AuthenticationResponse> outboundAuthenticate(@RequestParam("code") String code){
        var result = authenticationService.outboundAuthenticate(code);
        return ApiResponse.success(result);
    }

    @PostMapping("/token")
    @Operation(summary = "User Login",
            description = "Authenticates a user with email and password, returns JWT access and refresh tokens.")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.success(result);
    }


    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh Access Token",
            description = "Generates a new pair of access and refresh tokens using a valid refresh token.")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.success(result);
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "User Logout",
            description = "Invalidates the user's access token by adding it to a blacklist. Requires a valid access token in the Authorization header.")
    ApiResponse<String> logout(@RequestBody LogoutRequest request){
        authenticationService.logout(request);
        return ApiResponse.success("Log out successfully");
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot Password",
            description = "Initiates the password reset process by sending an OTP to the user's email.")
    public ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return ApiResponse.successWithMessage(authenticationService.forgotPassword(request));
    }


    @PostMapping("/verify-email")
    @Operation(summary = "Verify User Email",
            description = "Verifies a user's email address using the token sent during registration.")
    public ApiResponse<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        return ApiResponse.successWithMessage(authenticationService.verifyEmail(request));
    }


    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP",
            description = "Verifies the OTP sent for password reset.")
    public ApiResponse<Void> verifyOtp(@RequestBody VerifyOtpRequest request) {
        return ApiResponse.successWithMessage(authenticationService.verifyOtp(request));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change Password",
            description = "Changes the user's password after a successful OTP verification.")
    public ApiResponse<Void> changePassword(@RequestBody ChangePasswordRequest request) {
        return ApiResponse.successWithMessage(authenticationService.changePassword(request));
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> registerStudent(@Valid @RequestBody UserCreationRequest request) {
        return ApiResponse.success(authenticationService.register(request));
    }


}
