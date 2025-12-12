package com.fa25se225.capstone.mapper;

import com.fa25se225.capstone.dto.PaymentMethodDTO;
import com.fa25se225.capstone.entity.PaymentMethod;

public class PaymentMethodMapper {
    public static PaymentMethodDTO toDTO(PaymentMethod entity) {
        if (entity == null) return null;
        PaymentMethodDTO dto = new PaymentMethodDTO();
        dto.setId(entity.getId());
        dto.setBankingNumber(entity.getBankingNumber());
        dto.setNameBanking(entity.getNameBanking());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setAuthorName(entity.getAuthorName());
        return dto;
    }
}
