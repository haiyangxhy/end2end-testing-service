package com.testplatform.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "test_executions")
public class TestExecution {
    private static final Logger logger = LoggerFactory.getLogger(TestExecution.class);
    
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
    
    @Column(name = "environment_id", nullable = false)
    private String environmentId;
    
    @Column(name = "execution_type", nullable = false)
    private String executionType = "MANUAL";
    
    @Column(name = "triggered_by", nullable = false)
    private String triggeredBy;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "total_tests")
    private Integer totalTests = 0;
    
    @Column(name = "passed_tests")
    private Integer passedTests = 0;
    
    @Column(name = "failed_tests")
    private Integer failedTests = 0;
    
    @Column(name = "skipped_tests")
    private Integer skippedTests = 0;
    
    @Column(name = "execution_log", columnDefinition = "TEXT")
    private String executionLog;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "test_suite_name")
    private String testSuiteName;
    
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    @Column(name = "progress")
    private Integer progress;
    
    @Column(name = "duration")
    private Long duration;
    
    // Constructors
    public TestExecution() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
    }
    
    public TestExecution(String id, String suiteId, ExecutionStatus status) {
        this.id = id;
        this.suiteId = suiteId;
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
    
    public String getEnvironmentId() {
        return environmentId;
    }
    
    public void setEnvironmentId(String environmentId) {
        this.environmentId = environmentId;
    }
    
    public String getExecutionType() {
        return executionType;
    }
    
    public void setExecutionType(String executionType) {
        this.executionType = executionType;
    }
    
    public String getTriggeredBy() {
        return triggeredBy;
    }
    
    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }
    
    public Integer getDurationSeconds() {
        return durationSeconds;
    }
    
    public void setDurationSeconds(Integer durationSeconds) {
        this.durationSeconds = durationSeconds;
    }
    
    public Integer getTotalTests() {
        return totalTests;
    }
    
    public void setTotalTests(Integer totalTests) {
        this.totalTests = totalTests;
    }
    
    public Integer getPassedTests() {
        return passedTests;
    }
    
    public void setPassedTests(Integer passedTests) {
        this.passedTests = passedTests;
    }
    
    public Integer getFailedTests() {
        return failedTests;
    }
    
    public void setFailedTests(Integer failedTests) {
        this.failedTests = failedTests;
    }
    
    public Integer getSkippedTests() {
        return skippedTests;
    }
    
    public void setSkippedTests(Integer skippedTests) {
        this.skippedTests = skippedTests;
    }
    
    public String getExecutionLog() {
        return executionLog;
    }
    
    public void setExecutionLog(String executionLog) {
        this.executionLog = executionLog;
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
    
    public String getTestSuiteName() {
        return testSuiteName;
    }
    
    public void setTestSuiteName(String testSuiteName) {
        this.testSuiteName = testSuiteName;
    }
    
    public String getResult() {
        return result;
    }
    
    public void setResult(String result) {
        this.result = result;
    }
    
    public Integer getProgress() {
        return progress;
    }
    
    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    public Long getDuration() {
        return duration;
    }
    
    public void setDuration(Long duration) {
        this.duration = duration;
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
    
    public enum ExecutionStatus {
        PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
    }
}