package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.LinkStudentRequest;
import com.fa25se225.capstone.dto.request.PageResponse;
import com.fa25se225.capstone.dto.request.ParentProfileUpdateRequest;
import com.fa25se225.capstone.dto.request.UnlinkStudentRequest;
import com.fa25se225.capstone.dto.response.ChildOverviewResponse;
import com.fa25se225.capstone.dto.v2.response.ExamAttemptV2Response;

import java.util.List;

public interface ParentService {
    void linkStudent(LinkStudentRequest request);
    void unlinkStudent(UnlinkStudentRequest request);
    List<ChildOverviewResponse> getChildrenOverview();
    PageResponse<List<ExamAttemptV2Response>> getChildExamHistory(String studentId, int pageNo, int pageSize, String... sorts);

    void updateProfile(ParentProfileUpdateRequest request);
}