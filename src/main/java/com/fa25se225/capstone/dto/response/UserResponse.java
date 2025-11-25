package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Builder
public record UserResponse (
    String id,
    String email,
    String firstName,
    String lastName,
    String imgUrl,
    LocalDate dob,
    Set<String> roles,
    TeacherProfileResponse teacherProfile,
    StudentProfileResponse studentProfile,
    ParentProfileResponse parentProfile
){}
