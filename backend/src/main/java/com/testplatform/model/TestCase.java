package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "test_cases")
public class TestCase {
    private static final Logger logger = LoggerFactory.getLogger(TestCase.class);
    
    @Id
    private String id;
    
    @Column(name = "suite_id", nullable = false)
    private String suiteId;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "test_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TestCaseType type;
    
    @Column(columnDefinition = "JSONB")
    private String config;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public TestCase() {}
    
    public TestCase(String id, String suiteId, String name, String description, TestCaseType type, String config) {
        this.id = id;
        this.suiteId = suiteId;
        this.name = name;
        this.description = description;
        this.type = type;
        this.config = config;
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
    
    public String getSuiteId() {
        return suiteId;
    }
    
    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
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
    
    public TestCaseType getType() {
        return type;
    }
    
    public void setType(TestCaseType type) {
        this.type = type;
    }
    
    public String getConfig() {
        return config;
    }
    
    public void setConfig(String config) {
        this.config = config;
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
        logger.info("generateId() called, current id: {}", this.id);
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
            logger.info("Generated new ID: {}", this.id);
        } else {
            logger.info("ID already exists: {}", this.id);
        }
    }
    
    public enum TestCaseType {
        API, UI, BUSINESS
    }
}