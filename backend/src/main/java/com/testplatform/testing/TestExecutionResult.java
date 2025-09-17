package com.testplatform.testing;

public class TestExecutionResult {
    private boolean success;
    private String message;
    private long executionTime;
    
    public TestExecutionResult(boolean success, String message, long executionTime) {
        this.success = success;
        this.message = message;
        this.executionTime = executionTime;
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
}