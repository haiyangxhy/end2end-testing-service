package com.testplatform.controller;

import com.testplatform.model.TargetSystemConfig;
import com.testplatform.service.TargetSystemConfigService;
import com.testplatform.service.TargetSystemConfigServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/target-system-configs")
@CrossOrigin(origins = "*")
public class TargetSystemConfigController {
    
    @Autowired
    private TargetSystemConfigService configService;
    
    @GetMapping
    public ResponseEntity<List<TargetSystemConfig>> getAllConfigs() {
        List<TargetSystemConfig> configs = configService.getAllConfigs();
        return new ResponseEntity<>(configs, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TargetSystemConfig> getConfigById(@PathVariable String id) {
        Optional<TargetSystemConfig> config = configService.getConfigById(id);
        if (config.isPresent()) {
            return new ResponseEntity<>(config.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TargetSystemConfig> createConfig(@RequestBody TargetSystemConfig config) {
        TargetSystemConfig createdConfig = configService.createConfig(config);
        return new ResponseEntity<>(createdConfig, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TargetSystemConfig> updateConfig(@PathVariable String id, @RequestBody TargetSystemConfig config) {
        TargetSystemConfig updatedConfig = configService.updateConfig(id, config);
        if (updatedConfig != null) {
            return new ResponseEntity<>(updatedConfig, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteConfig(@PathVariable String id) {
        configService.deleteConfig(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}