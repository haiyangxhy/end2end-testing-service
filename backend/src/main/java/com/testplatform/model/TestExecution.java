package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_executions")
public class TestExecution {
    @Id
    private String id;
    
    @Column(name = "suite_id", nullable = false)
    private String suiteId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExecutionStatus status;
    
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(columnDefinition = "JSONB")
    private String result;
    
    // Constructors
    public TestExecution() {}
    
    public TestExecution(String id, String suiteId, ExecutionStatus status) {
        this.id = id;
        this.suiteId = suiteId;
        this.status = status;
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
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public enum ExecutionStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }
}