package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.CertificateDTO;
import com.fa25se225.capstone.entity.Certificate;
import com.fa25se225.capstone.entity.LearningMaterial;
import com.fa25se225.capstone.entity.User;
import com.fa25se225.capstone.repository.CertificateRepository;
import com.fa25se225.capstone.repository.LearningMaterialRepository;
import com.fa25se225.capstone.service.CertificateService;
import com.fa25se225.capstone.utils.AccountUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {
    private final CertificateRepository certificateRepository;
    private final LearningMaterialRepository learningMaterialRepository;
    private final AccountUtil accountUtil;

    @Override
    @Transactional
    public void createCertificate(User student, User teacher, String materialId) {
        LearningMaterial material = learningMaterialRepository.findById(materialId)
            .orElseThrow(() -> new RuntimeException("Learning material not found"));
        Certificate certificate = Certificate.builder()
                .user(student)
                .subject(material.getSubject())
                .certificateNumber(UUID.randomUUID().toString())
                .issueDate(LocalDate.now())
                .isValid(true)
                .deleted(false)
                .materialId(materialId)
                .build();
        certificateRepository.save(certificate);
    }

    @Override
    public List<CertificateDTO> getCertificatesByUserId(String userId) {
        List<Certificate> certificates = certificateRepository.findAll().stream()
            .filter(c -> c.getUser() != null && userId.equals(c.getUser().getId()))
            .collect(Collectors.toList());
        return certificates.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<CertificateDTO> getCertificatesForCurrentUser() {
        User user = accountUtil.getCurrentUser();
        return getCertificatesByUserId(user.getId());
    }

    @Override
    public CertificateDTO getCertificateById(String certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
            .orElseThrow(() -> new RuntimeException("Certificate not found"));
        return toDTO(certificate);
    }

    private CertificateDTO toDTO(Certificate entity) {
        CertificateDTO dto = new CertificateDTO();
        dto.setId(entity.getId());
        dto.setCertificateNumber(entity.getCertificateNumber());
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId() : null);
        dto.setSubjectId(entity.getSubject() != null ? entity.getSubject().getId() : null);
        dto.setIssueDate(entity.getIssueDate());
        dto.setIsValid(entity.getIsValid());
        dto.setCertificateUrl(entity.getCertificateUrl());
        dto.setMaterialId(entity.getMaterialId());
        return dto;
    }
}
