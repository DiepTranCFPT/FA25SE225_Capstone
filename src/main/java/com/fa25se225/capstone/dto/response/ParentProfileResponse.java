package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.entity.StudentProfile;
import com.fa25se225.capstone.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ParentProfileResponse {
    private String id;
    private String occupation;
    private String address;
}
