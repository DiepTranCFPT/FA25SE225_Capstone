package com.fa25se225.capstone.service;

import com.fa25se225.capstone.entity.Certificate;
import com.fa25se225.capstone.entity.User;

public interface CertificateService {
    Certificate createCertificate(User student, User teacher, String materialId);
}

