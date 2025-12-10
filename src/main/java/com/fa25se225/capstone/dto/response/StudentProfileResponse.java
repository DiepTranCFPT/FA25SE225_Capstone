package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.entity.AdvisorProfile;
import com.fa25se225.capstone.entity.GradeLevel;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class StudentProfileResponse {
    private String id;
    private String schoolName;
    private String emergencyContact;
    private String goal;


}
