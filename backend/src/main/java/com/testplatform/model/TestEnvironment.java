package com.testplatform.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_environments")
public class TestEnvironment {
    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "name", unique = true, nullable = false)
    @NotBlank(message = "环境名称不能为空")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "api_base_url")
    private String apiBaseUrl;

    @Column(name = "ui_base_url")
    private String uiBaseUrl;

    @Column(name = "database_config", columnDefinition = "TEXT")
    private String databaseConfig;

    @Column(name = "auth_config", columnDefinition = "TEXT")
    private String authConfig;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public TestEnvironment() {}

    public TestEnvironment(String name, String description) {
        this.name = name;
        this.description = description;
        this.isActive = false;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    public String getUiBaseUrl() {
        return uiBaseUrl;
    }

    public void setUiBaseUrl(String uiBaseUrl) {
        this.uiBaseUrl = uiBaseUrl;
    }

    public String getDatabaseConfig() {
        return databaseConfig;
    }

    public void setDatabaseConfig(String databaseConfig) {
        this.databaseConfig = databaseConfig;
    }

    public String getAuthConfig() {
        return authConfig;
    }

    public void setAuthConfig(String authConfig) {
        this.authConfig = authConfig;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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
