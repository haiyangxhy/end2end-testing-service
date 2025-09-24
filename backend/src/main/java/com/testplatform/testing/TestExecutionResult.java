package com.testplatform.testing;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TestExecutionResult {
    private boolean success;
    private String message;
    private long executionTime;
    private String testCaseId;
    private String testCaseName;
    private String testType;
    private Map<String, Object> metadata = new HashMap<>();
    private String startTime;
    private String endTime;
    private String errorDetails;

    public TestExecutionResult(){}
    
    public TestExecutionResult(boolean success, String message, long executionTime) {
        this.success = success;
        this.message = message;
        this.executionTime = executionTime;
        this.startTime = LocalDateTime.now().toString();
        this.endTime = LocalDateTime.now().toString();
    }
    
    public TestExecutionResult(boolean success, String message, long executionTime, 
                             String testCaseId, String testCaseName, String testType) {
        this.success = success;
        this.message = message;
        this.executionTime = executionTime;
        this.testCaseId = testCaseId;
        this.testCaseName = testCaseName;
        this.testType = testType;
        this.startTime = LocalDateTime.now().toString();
        this.endTime = LocalDateTime.now().toString();
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public long getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }
    
    public String getTestCaseId() {
        return testCaseId;
    }
    
    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }
    
    public String getTestCaseName() {
        return testCaseName;
    }
    
    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }
    
    public String getTestType() {
        return testType;
    }
    
    public void setTestType(String testType) {
        this.testType = testType;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    public String getStartTime() {
        return startTime;
    }
    
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }
    
    public String getEndTime() {
        return endTime;
    }
    
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
    
    public String getErrorDetails() {
        return errorDetails;
    }
    
    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }
    
    @Override
    public String toString() {
        return "TestExecutionResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", executionTime=" + executionTime +
                ", testCaseId='" + testCaseId + '\'' +
                ", testCaseName='" + testCaseName + '\'' +
                ", testType='" + testType + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", errorDetails='" + errorDetails + '\'' +
                '}';
    }
}