package com.fa25se225.capstone.dto.v2.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SubjectV2Response {
    private String id;
    private String name;
    private String description;
}
