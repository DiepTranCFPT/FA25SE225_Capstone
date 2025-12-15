package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.entity.Subject;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommunityResponse {
    private String name;
    private String description;
    private String imgUrl;
    private String subject;
}
