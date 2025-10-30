package com.fa25se225.capstone.dto.response;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class ExamResponse {
    private String id;
    private String title;
    private String description;
    private Integer duration;
    private Integer passingScore;
    private List<String> subjectNames;
    private String createdByName;
    private List<String> questionContents;
    private Boolean isActive;
    private LocalDate createdAt;
}
