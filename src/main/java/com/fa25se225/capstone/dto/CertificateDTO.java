package com.fa25se225.capstone.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CertificateDTO {
    private String id;
    private String certificateNumber;
    private String userId;
    private String subjectId;
    private LocalDate issueDate;
    private Boolean isValid;
    private String certificateUrl;
}

