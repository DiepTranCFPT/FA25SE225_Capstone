package com.fa25se225.capstone.repository;

import com.fa25se225.capstone.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
}

