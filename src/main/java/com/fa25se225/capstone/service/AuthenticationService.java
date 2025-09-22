package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.AuthenticationRequest;
import com.fa25se225.capstone.dto.request.IntrospectRequest;
import com.fa25se225.capstone.dto.request.LogoutRequest;
import com.fa25se225.capstone.dto.request.RefreshRequest;
import com.fa25se225.capstone.dto.response.AuthenticationResponse;
import com.fa25se225.capstone.dto.response.IntrospectResponse;
import com.nimbusds.jose.JOSEException;

import java.text.ParseException;

public interface AuthenticationService {
    AuthenticationResponse authenticate(AuthenticationRequest request);
    IntrospectResponse introspect(IntrospectRequest request);
    AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException;
    AuthenticationResponse outboundAuthenticate(String code);
    void logout(LogoutRequest request) throws ParseException, JOSEException;
}
