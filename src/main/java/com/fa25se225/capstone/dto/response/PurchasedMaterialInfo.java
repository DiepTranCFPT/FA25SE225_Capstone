package com.fa25se225.capstone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
