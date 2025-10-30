package com.fa25se225.capstone.dto.request;

import java.util.List;

import lombok.Data;

@Data
public class ExamCreationRequest {
    private String title;
    private String description;
    private Integer duration;
    private Integer passingScore;
    private List<String> subjectIds;
    private List<String> questionIds;
    private Boolean isActive;
}
