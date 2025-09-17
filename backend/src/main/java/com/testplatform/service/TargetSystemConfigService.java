package com.testplatform.service;

import com.testplatform.model.TargetSystemConfig;
import java.util.List;
import java.util.Optional;

public interface TargetSystemConfigService {
    List<TargetSystemConfig> getAllConfigs();
    List<TargetSystemConfig> getActiveConfigs();
    Optional<TargetSystemConfig> getConfigById(String id);
    TargetSystemConfig createConfig(TargetSystemConfig config);
    TargetSystemConfig updateConfig(String id, TargetSystemConfig config);
    void deleteConfig(String id);
}