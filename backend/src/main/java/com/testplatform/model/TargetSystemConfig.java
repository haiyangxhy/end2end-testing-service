package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "target_system_configs")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TargetSystemConfig {
    private static final Logger logger = LoggerFactory.getLogger(TargetSystemConfig.class);
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(name = "api_url", nullable = false)
    private String apiUrl;
    
    @Column(name = "ui_url")
    private String uiUrl;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public TargetSystemConfig() {}
    
    public TargetSystemConfig(String id, String name, String apiUrl, String uiUrl, String description, boolean isActive) {
        this.id = id;
        this.name = name;
        this.apiUrl = apiUrl;
        this.uiUrl = uiUrl;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getUiUrl() {
        return uiUrl;
    }
    
    public void setUiUrl(String uiUrl) {
        this.uiUrl = uiUrl;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @JsonProperty("isActive")
    public boolean isActive() {
        logger.info("Getting isActive value: {}", isActive);
        return isActive;
    }
    
    @JsonProperty("isActive")
    public void setActive(boolean active) {
        logger.info("Setting isActive value: {}", active);
        isActive = active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}