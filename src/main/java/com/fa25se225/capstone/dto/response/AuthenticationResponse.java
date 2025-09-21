package com.fa25se225.capstone.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;


@Builder
public record AuthenticationResponse (
    String token,
    boolean authenticated
){}
