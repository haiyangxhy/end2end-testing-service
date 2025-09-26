package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_tasks")
public class ScheduledTask {
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "suite_id", nullable = false)
    private String suiteId;
    
    @Column(name = "environment_id", nullable = false)
    private String environmentId;
    
    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "last_run")
    private LocalDateTime lastRun;
    
    @Column(name = "next_run")
    private LocalDateTime nextRun;
    
    @Column(name = "created_by", nullable = false)
    private String createdBy;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public ScheduledTask() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ScheduledTask(String name, String suiteId, String environmentId, String cronExpression) {
        this();
        this.name = name;
        this.suiteId = suiteId;
        this.environmentId = environmentId;
        this.cronExpression = cronExpression;
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
    
    public String getSuiteId() {
        return suiteId;
    }
    
    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }
    
    public String getEnvironmentId() {
        return environmentId;
    }
    
    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }
    
    public String getCronExpression() {
        return cronExpression;
    }
    
    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getLastRun() {
        return lastRun;
    }
    
    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }
    
    public LocalDateTime getNextRun() {
        return nextRun;
    }
    
    public void setNextRun(LocalDateTime nextRun) {
        this.nextRun = nextRun;
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
    
    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}
