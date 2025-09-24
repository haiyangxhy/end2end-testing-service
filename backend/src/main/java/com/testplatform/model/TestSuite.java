package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "test_suites")
public class TestSuite {
    private static final Logger logger = LoggerFactory.getLogger(TestSuite.class);
    
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "suite_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TestSuiteType type;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @ElementCollection
    @CollectionTable(name = "test_suite_cases", joinColumns = @JoinColumn(name = "suite_id"))
    @Column(name = "test_case_id")
    private List<String> testCases;
    
    // Constructors
    public TestSuite() {}
    
    public TestSuite(String id, String name, String description, TestSuiteType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
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
    
    public TestSuiteType getType() {
        return type;
    }
    
    public void setType(TestSuiteType type) {
        this.type = type;
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
    
    public List<String> getTestCases() {
        return testCases;
    }
    
    public void setTestCases(List<String> testCases) {
        this.testCases = testCases;
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
    
    public enum TestSuiteType {
        API, UI, BUSINESS
    }
}