package com.fa25se225.capstone.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class PurchasedMaterialInfo {
    private String materialId;
    private String title;
    private String subjectName;
    private String typeName;
    private BigDecimal price;
    private LocalDate purchasedDate;
    private String authorName;
    private String fileImage;
}
