package com.fa25se225.capstone.dto.response;

import java.util.List;

public record LearningMaterialWithStudentsResponse(
    LearningMaterialResponse material,
    List<UserResponse> registeredStudents
) {}

