package com.fa25se225.capstone.dto.request;


import lombok.Data;

@Data
public class StudentProfileUpdateRequest {
    private String schoolName;
    private String parentPhone;
    private String emergencyContact;
    private String goal;
}
