package com.testplatform.service;

import com.testplatform.model.TargetSystemConfig;
import com.testplatform.repository.TargetSystemConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
public class TargetSystemConfigServiceImpl implements TargetSystemConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(TargetSystemConfigServiceImpl.class);
    
    @Autowired
    private TargetSystemConfigRepository configRepository;
    
    @Override
    public List<TargetSystemConfig> getAllConfigs() {
        return configRepository.findAll();
    }
    
    @Override
    public List<TargetSystemConfig> getActiveConfigs() {
        return configRepository.findByIsActiveTrue();
    }
    
    @Override
    public Optional<TargetSystemConfig> getConfigById(String id) {
        return configRepository.findById(id);
    }
    
    @Override
    public TargetSystemConfig createConfig(TargetSystemConfig config) {
        logger.info("Creating config with isActive: {}", config.isActive());
        if (config.getId() == null || config.getId().isEmpty()) {
            config.setId(UUID.randomUUID().toString());
        }
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        logger.info("Saving config with isActive: {}", config.isActive());
        return configRepository.save(config);
    }
    
    @Override
    public TargetSystemConfig updateConfig(String id, TargetSystemConfig config) {
        logger.info("Updating config with isActive: {}", config.isActive());
        Optional<TargetSystemConfig> existingConfig = configRepository.findById(id);
        if (existingConfig.isPresent()) {
            TargetSystemConfig updatedConfig = existingConfig.get();
            updatedConfig.setName(config.getName());
            updatedConfig.setApiUrl(config.getApiUrl());
            updatedConfig.setUiUrl(config.getUiUrl());
            updatedConfig.setDescription(config.getDescription());
            updatedConfig.setActive(config.isActive());
            updatedConfig.setUpdatedAt(LocalDateTime.now());
            logger.info("Saving updated config with isActive: {}", updatedConfig.isActive());
            return configRepository.save(updatedConfig);
        }
        return null;
    }
    
    @Override
    public void deleteConfig(String id) {
        configRepository.deleteById(id);
    }
}