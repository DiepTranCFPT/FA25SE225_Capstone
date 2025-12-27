package com.fa25se225.capstone.service.implementation;

import com.fa25se225.capstone.dto.request.UpdatePercentagesRequest;
import com.fa25se225.capstone.entity.PercentagesConfig;
import com.fa25se225.capstone.repository.PercentagesConfigRepository;
import com.fa25se225.capstone.service.PercentagesConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PercentagesConfigServiceImpl implements PercentagesConfigService {
    @Autowired
    private PercentagesConfigRepository percentagesConfigRepository;

    @Override
    public PercentagesConfig getConfig() {
        List<PercentagesConfig> configs = percentagesConfigRepository.findAll();
        if (configs.isEmpty()) {
            return null;
        }
        return configs.get(0);
    }

    @Override
    public PercentagesConfig createConfig(UpdatePercentagesRequest request) {
        if (!isValid(request)) {
            throw new IllegalArgumentException("Invalid percentages");
        }
        PercentagesConfig config = new PercentagesConfig();
        config.setPercentTeacherVerified(request.getPercentTeacher());
        config.setPercentAdminVerified(BigDecimal.ONE.subtract(request.getPercentTeacher()));
        config.setPercentTeacherUnverified(request.getPercentTeacherUnverified());
        config.setPercentAdminUnverified(BigDecimal.ONE.subtract(request.getPercentTeacherUnverified()));
        return percentagesConfigRepository.save(config);
    }

    @Override
    public PercentagesConfig updateConfig(UpdatePercentagesRequest request) {
        List<PercentagesConfig> configs = percentagesConfigRepository.findAll();
        if (configs.isEmpty()) {
            throw new IllegalStateException("No config to update");
        }
        if (!isValid(request)) {
            throw new IllegalArgumentException("Invalid percentages");
        }
        PercentagesConfig config = configs.get(0);
        config.setPercentTeacherVerified(request.getPercentTeacher());
        config.setPercentAdminVerified(BigDecimal.ONE.subtract(request.getPercentTeacher()));
        config.setPercentTeacherUnverified(request.getPercentTeacherUnverified());
        config.setPercentAdminUnverified(BigDecimal.ONE.subtract(request.getPercentTeacherUnverified()));
        return percentagesConfigRepository.save(config);
    }

    @Override
    public boolean isValid(UpdatePercentagesRequest req) {
        if (req.getPercentTeacher() == null || req.getPercentTeacherUnverified() == null) {
            return false;
        }
        boolean verifiedRange = req.getPercentTeacher().compareTo(BigDecimal.ZERO) >= 0 && req.getPercentTeacher().compareTo(BigDecimal.ONE) <= 0;
        boolean unverifiedRange = req.getPercentTeacherUnverified().compareTo(BigDecimal.ZERO) >= 0 && req.getPercentTeacherUnverified().compareTo(BigDecimal.ONE) <= 0;
        return verifiedRange && unverifiedRange;
    }
}
