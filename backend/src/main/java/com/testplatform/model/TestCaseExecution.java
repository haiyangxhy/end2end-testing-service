package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_case_executions")
public class TestCaseExecution {
    
    @Id
    private String id;
    
    @Column(name = "execution_id", nullable = false)
    private String executionId;
    
    @Column(name = "test_case_id", nullable = false)
    private String testCaseId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(name = "duration_ms")
    private Integer durationMs;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;
    
    @Column(name = "response_data", columnDefinition = "JSONB")
    private String responseData;
    
    @Column(name = "extracted_variables", columnDefinition = "JSONB")
    private String extractedVariables;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public TestCaseExecution() {}
    
    public TestCaseExecution(String id, String executionId, String testCaseId, ExecutionStatus status) {
        this.id = id;
        this.executionId = executionId;
        this.testCaseId = testCaseId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
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
    
    public ExecutionStatus getStatus() {
        return status;
    }
    
    public void setStatus(ExecutionStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public Integer getDurationMs() {
        return durationMs;
    }
    
    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    public String getResponseData() {
        return responseData;
    }
    
    public void setResponseData(String responseData) {
        this.responseData = responseData;
    }
    
    public String getExtractedVariables() {
        return extractedVariables;
    }
    
    public void setExtractedVariables(String extractedVariables) {
        this.extractedVariables = extractedVariables;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public enum ExecutionStatus {
        PENDING, RUNNING, PASSED, FAILED, SKIPPED, ERROR
    }
}
