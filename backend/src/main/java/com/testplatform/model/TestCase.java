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
    
    // 移除suiteId字段，因为测试用例现在通过test_suite_cases表关联
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "test_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TestCaseType type;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Priority priority = Priority.MEDIUM;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String config;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "test_steps", columnDefinition = "TEXT")
    private String testSteps;
    
    @Column(name = "expected_result", columnDefinition = "TEXT")
    private String expectedResult;
    
    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public TestCase() {}
    
    public TestCase(String id, String name, String description, TestCaseType type, Priority priority, String config) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.priority = priority;
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
    
    // 移除suiteId相关的getter/setter
    
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
    
    public Priority getPriority() {
        return priority;
    }
    
    public void setPriority(Priority priority) {
        this.priority = priority;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public String getTestSteps() {
        return testSteps;
    }
    
    public void setTestSteps(String testSteps) {
        this.testSteps = testSteps;
    }
    
    public String getExpectedResult() {
        return expectedResult;
    }
    
    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
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
    
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    public enum Status {
        ACTIVE, INACTIVE, DRAFT
    }
}