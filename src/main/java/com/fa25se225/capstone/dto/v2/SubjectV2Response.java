package com.fa25se225.capstone.dto.v2;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectV2Response {
    private String id;
    private String name;
}