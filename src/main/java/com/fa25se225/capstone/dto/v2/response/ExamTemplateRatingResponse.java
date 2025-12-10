package com.fa25se225.capstone.dto.v2.response;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ExamTemplateRatingResponse {
    private Integer rating;
    private String comment;
    private Student rateBy;
    private LocalDateTime ratingTime;

    @Builder
    public record Student(
            String id,
            String email,
            String firstName,
            String lastName,
            String imgUrl,
            LocalDate dob
    ){
    }

}
