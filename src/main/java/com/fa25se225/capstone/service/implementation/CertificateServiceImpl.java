package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.entity.Certificate;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.CertificateRepository;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.service.CertificateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    @Override
    @Transactional
    public Certificate createCertificate(User student, User teacher, String materialId) {
        LearningMaterial material = learningMaterialRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Learning material not found"));
        Certificate certificate = Certificate.builder()
                .user(student)
                .subject(material.getSubject())
                .certificateNumber(UUID.randomUUID().toString())
                .issueDate(LocalDate.now())
                .isValid(true)
                .deleted(false)
                .build();
        return certificateRepository.save(certificate);
    }
}
