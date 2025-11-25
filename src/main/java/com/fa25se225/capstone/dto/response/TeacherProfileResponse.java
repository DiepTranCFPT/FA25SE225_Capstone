package com.fa25se225.capstone.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TeacherProfileResponse {
    private String id;
    private String qualification;
    private String specialization;
    private String experience;
    private String biography;
    private Integer rating;
    private List<String> certificateUrls;
    private Boolean isVerified;
}
