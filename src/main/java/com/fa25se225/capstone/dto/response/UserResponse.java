package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.dto.response.TeacherProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String imgUrl;
    private LocalDate dob;
    private Set<String> roles;
    private TeacherProfileResponse teacherProfile;
    private StudentProfileResponse studentProfile;
    private ParentProfileResponse parentProfile;
}