package com.fa25se225.capstone.mapper.v2;

import com.fa25se225.capstone.dto.v2.SuspiciousActivityLogResponse;
import com.fa25se225.capstone.entity.v2.SuspiciousActivityLog;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SuspiciousActivityLogMapper {
    SuspiciousActivityLogResponse toResponse(SuspiciousActivityLog entity);
    List<SuspiciousActivityLogResponse> toListResponses(List<SuspiciousActivityLog> entities);

}
