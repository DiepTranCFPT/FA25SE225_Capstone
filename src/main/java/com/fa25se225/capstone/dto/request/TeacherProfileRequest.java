package com.fa25se225.capstone.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TeacherProfileRequest {
    private LocalDate dateOfBirth;
    private String qualification;
    private String specialization;
    private String experience;
    private String biography;
    private List<String> certificateUrls;
}
