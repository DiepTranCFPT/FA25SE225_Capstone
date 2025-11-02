package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.constant.PredefinedSystemPermission;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {

    @GetMapping("/ping")
    @PreAuthorize("hasAuthority('TEST_PERMISSION')")
    public String ping(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Ok";
    }

    @GetMapping("/ping2")
    @PreAuthorize("hasAuthority('PING')")
    public String ping2(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Ok";
    }

    @GetMapping("/ping-role")
    @PreAuthorize("hasRole('ADMIN')")
    public String ping3(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "Ok";
    }
}
