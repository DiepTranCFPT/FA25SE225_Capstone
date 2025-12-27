package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.SubjectProgressDto;

import java.util.List;

public interface ParentProgressService {

    List<SubjectProgressDto> getChildSubjectProgress(String parentId, String studentId);

    List<SubjectProgressDto> getChildCompletedSubjects(String parentId, String studentId);
}

