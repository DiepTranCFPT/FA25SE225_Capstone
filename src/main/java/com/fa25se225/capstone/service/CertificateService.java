package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.CertificateDTO;
import com.fa25se225.capstone.entity.User;
import java.util.List;

public interface CertificateService {
    void createCertificate(User student, User teacher, String materialId);
    List<CertificateDTO> getCertificatesByUserId(String userId);
    List<CertificateDTO> getCertificatesForCurrentUser();
    CertificateDTO getCertificateById(String certificateId);
}
