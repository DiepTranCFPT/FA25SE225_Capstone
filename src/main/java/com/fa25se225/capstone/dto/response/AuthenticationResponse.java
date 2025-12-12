package com.fa25se225.capstone.dto.response;

import com.fa25se225.capstone.entity.Role;
import com.fa25se225.capstone.entity.User;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.Set;


@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {

    String token;
    boolean authenticated;
    List<String> roles;


    public static AuthenticationResponse build(String token, boolean authenticated, Set<Role> roles){
         return AuthenticationResponse.builder()
                    .token(token)
                    .authenticated(true)
                    .roles(roles.stream().map(role -> role.getName()).toList()).build();

    };

}
