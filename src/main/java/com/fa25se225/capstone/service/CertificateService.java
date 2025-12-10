package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.User;

public interface CertificateService {
    void createCertificate(User student, User teacher, String materialId);
}

