package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "test_execution_logs")
public class TestExecutionLog {
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "execution_id", nullable = false)
    private String executionId;
    
    @Column(name = "test_case_id", nullable = false)
    private String testCaseId;
    
    @Column(name = "step", nullable = false)
    private String step;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "level", nullable = false)
    private String level;
    
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "request_data", columnDefinition = "TEXT")
    private String requestData;
    
    @Column(name = "response_data", columnDefinition = "TEXT")
    private String responseData;
    
    @Column(name = "duration")
    private Long duration;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TestExecutionLog() {
        this.timestamp = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
    }
    
    public TestExecutionLog(String executionId, String testCaseId, String step, String level, String message) {
        this();
        this.executionId = executionId;
        this.testCaseId = testCaseId;
        this.step = step;
        this.level = level;
        this.message = message;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getExecutionId() {
        return executionId;
    }
    
    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }
    
    public String getTestCaseId() {
        return testCaseId;
    }
    
    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }
    
    public String getStep() {
        return step;
    }
    
    public void setStep(String step) {
        this.step = step;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getLevel() {
        return level;
    }
    
    public void setLevel(String level) {
        this.level = level;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getRequestData() {
        return requestData;
    }
    
    public void setRequestData(String requestData) {
        this.requestData = requestData;
    }
    
    public String getResponseData() {
        return responseData;
    }
    
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @PrePersist
    public void generateId() {
        if (this.id == null || this.id.isEmpty()) {
            this.id = UUID.randomUUID().toString();
        }
    }
}
