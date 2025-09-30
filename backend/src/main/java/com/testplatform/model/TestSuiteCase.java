package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_suite_cases")
public class TestSuiteCase {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "suite_id", nullable = false)
    private String suiteId;
    
    @Column(name = "test_case_id", nullable = false)
    private String testCaseId;
    
    @Column(name = "execution_order", nullable = false)
    private Integer executionOrder;
    
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public TestSuiteCase() {
        this.createdAt = LocalDateTime.now();
    }
    
    public TestSuiteCase(String suiteId, String testCaseId, Integer executionOrder) {
        this();
        this.suiteId = suiteId;
        this.testCaseId = testCaseId;
        this.executionOrder = executionOrder;
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
    
    public String getTestCaseId() {
        return testCaseId;
    }
    
    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }
    
    public Integer getExecutionOrder() {
        return executionOrder;
    }
    
    public void setExecutionOrder(Integer executionOrder) {
        this.executionOrder = executionOrder;
    }
    
    public Boolean getIsEnabled() {
        return isEnabled;
    }
    
    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
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
