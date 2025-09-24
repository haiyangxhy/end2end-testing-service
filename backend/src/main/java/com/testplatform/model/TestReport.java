package com.testplatform.model;

import com.testplatform.util.ReportDetailListConverter;
import com.testplatform.util.ReportSummaryConverter;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "test_reports")
public class TestReport {
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "execution_id")
    private String executionId;
    
    @Column(name = "suite_id")
    private String suiteId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "summary", columnDefinition = "jsonb")
    @Convert(converter = ReportSummaryConverter.class)
    private ReportSummary summary;
    
    @Column(name = "details", columnDefinition = "jsonb")
    @Convert(converter = ReportDetailListConverter.class)
    private List<ReportDetail> details;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public TestReport() {}
    
    public TestReport(String id, String executionId, String suiteId, String name) {
        this.id = id;
        this.executionId = executionId;
        this.suiteId = suiteId;
        this.name = name;
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
    
    public String getSuiteId() {
        return suiteId;
    }
    
    public void setSuiteId(String suiteId) {
        this.suiteId = suiteId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public ReportSummary getSummary() {
        return summary;
    }
    
    public void setSummary(ReportSummary summary) {
        this.summary = summary;
    }
    
    public List<ReportDetail> getDetails() {
        return details;
    }
    
    public void setDetails(List<ReportDetail> details) {
        this.details = details;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    // Inner classes for report structure
    public static class ReportSummary {
        private int totalTests;
        private int passedTests;
        private int failedTests;
        private int skippedTests;
        private double passRate;
        private long averageResponseTime;
        private String startTime;
        private String endTime;
        
        // Constructors
        public ReportSummary() {}
        
        // Getters and Setters
        public int getTotalTests() {
            return totalTests;
        }
        
        public void setTotalTests(int totalTests) {
            this.totalTests = totalTests;
        }
        
        public int getPassedTests() {
            return passedTests;
        }
        
        public void setPassedTests(int passedTests) {
            this.passedTests = passedTests;
        }
        
        public int getFailedTests() {
            return failedTests;
        }
        
        public void setFailedTests(int failedTests) {
            this.failedTests = failedTests;
        }
        
        public int getSkippedTests() {
            return skippedTests;
        }
        
        public void setSkippedTests(int skippedTests) {
            this.skippedTests = skippedTests;
        }
        
        public double getPassRate() {
            return passRate;
        }
        
        public void setPassRate(double passRate) {
            this.passRate = passRate;
        }
        
        public long getAverageResponseTime() {
            return averageResponseTime;
        }
        
        public void setAverageResponseTime(long averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
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
    }
    
    public static class ReportDetail {
        private String testCaseId;
        private String testCaseName;
        private String testType;
        private String status;
        private String message;
        private String errorMessage;
        private String errorDetails;
        private long responseTime;
        private String startTime;
        private String endTime;
        private String timestamp;
        private String metadata;
        
        // Constructors
        public ReportDetail() {}
        
        // Getters and Setters
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
        
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
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
        
        public long getResponseTime() {
            return responseTime;
        }
        
        public void setResponseTime(long responseTime) {
            this.responseTime = responseTime;
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
        
        public String getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
        public String getMetadata() {
            return metadata;
        }
        
        public void setMetadata(String metadata) {
            this.metadata = metadata;
        }
    }
}