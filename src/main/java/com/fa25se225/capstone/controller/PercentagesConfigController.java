package com.fa25se225.capstone.controller;

import com.fa25se225.capstone.dto.request.UpdatePercentagesRequest;
import com.fa25se225.capstone.entity.PercentagesConfig;
import com.fa25se225.capstone.service.PercentagesConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/percentages-config")
public class PercentagesConfigController {
    @Autowired
    private PercentagesConfigService percentagesConfigService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PercentagesConfig> getConfig() {
        PercentagesConfig config = percentagesConfigService.getConfig();
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PercentagesConfig> createConfig(@RequestBody UpdatePercentagesRequest request) {
        if (!percentagesConfigService.isValid(request)) {
            return ResponseEntity.badRequest().build();
        }
        PercentagesConfig saved = percentagesConfigService.createConfig(request);
        return ResponseEntity.ok(saved);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PercentagesConfig> updateConfig(@RequestBody UpdatePercentagesRequest request) {
        if (!percentagesConfigService.isValid(request)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            PercentagesConfig saved = percentagesConfigService.updateConfig(request);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
