package com.fa25se225.capstone.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Map;


@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record EmailRequest(
        Sender sender,
        List<Recipient> to,
        Long templateId,
        Map<String, Object> params
){}

