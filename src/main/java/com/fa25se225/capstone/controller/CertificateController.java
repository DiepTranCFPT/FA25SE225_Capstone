package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.CertificateDTO;
import com.fa25se225.capstone.dto.response.ApiResponse;
import com.fa25se225.capstone.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
public class CertificateController {
    private final CertificateService certificateService;

    @GetMapping("/user/{userId}")
    public ApiResponse<List<CertificateDTO>> getCertificatesByUser(@PathVariable String userId) {
        return ApiResponse.success(certificateService.getCertificatesByUserId(userId));
    }

    @GetMapping("/me")
    public ApiResponse<List<CertificateDTO>> getCertificatesForCurrentUser() {
        return ApiResponse.success(certificateService.getCertificatesForCurrentUser());
    }

    @GetMapping("/{certificateId}")
    public ApiResponse<CertificateDTO> getCertificateById(@PathVariable String certificateId) {
        return ApiResponse.success(certificateService.getCertificateById(certificateId));
    }
}

