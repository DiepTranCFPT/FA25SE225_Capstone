package com.fa25se225.capstone.service;

import com.fa25se225.capstone.dto.request.UpdatePercentagesRequest;
import com.fa25se225.capstone.entity.PercentagesConfig;

public interface PercentagesConfigService {
    PercentagesConfig getConfig();
    PercentagesConfig createConfig(UpdatePercentagesRequest request);
    PercentagesConfig updateConfig(UpdatePercentagesRequest request);
    boolean isValid(UpdatePercentagesRequest request);
}
