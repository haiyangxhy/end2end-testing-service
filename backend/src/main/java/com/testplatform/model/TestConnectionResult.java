package com.testplatform.model;

/**
 * 连接测试结果类
 */
public class TestConnectionResult {
    private boolean success;
    private String message;
    
    public TestConnectionResult() {
    }
    
    public TestConnectionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters and Setters
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
}