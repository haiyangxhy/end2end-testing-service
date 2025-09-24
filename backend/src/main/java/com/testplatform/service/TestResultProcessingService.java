package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestReport;
import com.testplatform.testing.TestExecutionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试结果处理服务
 * 负责处理测试执行结果，生成测试报告，统计分析等
 */
@Service
public class TestResultProcessingService {
    private static final Logger logger = LoggerFactory.getLogger(TestResultProcessingService.class);
    
    @Autowired
    private TestReportGenerationService testReportGenerationService;
    
    /**
     * 处理测试执行结果
     */
    public void processTestResult(TestExecution execution, TestExecutionResult result) {
        logger.info("处理测试执行结果: {} - {}", execution.getId(), result.isSuccess() ? "成功" : "失败");
        
        try {
            // 更新执行状态
            updateExecutionStatus(execution, result);
            
            // 生成测试报告
            generateTestReport(execution, result);
            
            // 统计分析
            performStatisticalAnalysis(execution, result);
            
            // 发送通知（如果需要）
            sendNotificationIfNeeded(execution, result);
            
        } catch (Exception e) {
            logger.error("处理测试执行结果时出错", e);
        }
    }
    
    /**
     * 更新执行状态
     */
    private void updateExecutionStatus(TestExecution execution, TestExecutionResult result) {
        if (result.isSuccess()) {
            execution.setStatus(TestExecution.ExecutionStatus.COMPLETED);
        } else {
            execution.setStatus(TestExecution.ExecutionStatus.FAILED);
        }
        
        execution.setEndTime(LocalDateTime.now());
        execution.setResult(result.getMessage());
        
        logger.info("更新执行状态: {} - {}", execution.getId(), execution.getStatus());
    }
    
    /**
     * 生成测试报告
     */
    private void generateTestReport(TestExecution execution, TestExecutionResult result) {
        try {
            TestReport report = new TestReport();
            report.setId("report-" + System.currentTimeMillis());
            report.setExecutionId(execution.getId());
            report.setSuiteId(execution.getSuiteId());
            report.setName("测试报告 - " + execution.getId());
            
            // 设置报告摘要
            TestReport.ReportSummary summary = new TestReport.ReportSummary();
            summary.setTotalTests(1);
            summary.setPassedTests(result.isSuccess() ? 1 : 0);
            summary.setFailedTests(result.isSuccess() ? 0 : 1);
            summary.setSkippedTests(0);
            summary.setAverageResponseTime(result.getExecutionTime());
            summary.setPassRate(result.isSuccess() ? 100.0 : 0.0);
            report.setSummary(summary);
            
            // 设置报告详情
            List<TestReport.ReportDetail> details = new ArrayList<>();
            TestReport.ReportDetail detail = new TestReport.ReportDetail();
            detail.setTestCaseId(execution.getId());
            detail.setTestCaseName("测试执行 - " + execution.getId());
            detail.setStatus(result.isSuccess() ? "PASSED" : "FAILED");
            detail.setResponseTime(result.getExecutionTime());
            detail.setErrorMessage(result.getMessage());
            detail.setTimestamp(LocalDateTime.now().toString());
            details.add(detail);
            report.setDetails(details);
            
            report.setCreatedAt(LocalDateTime.now());
            
            // 保存报告
            // testReportGenerationService.saveReport(report);
            
            logger.info("生成测试报告: {}", report.getId());
        } catch (Exception e) {
            logger.error("生成测试报告时出错", e);
        }
    }
    
    /**
     * 执行统计分析
     */
    private void performStatisticalAnalysis(TestExecution execution, TestExecutionResult result) {
        try {
            // 这里可以添加更复杂的统计分析逻辑
            // 比如：成功率统计、执行时间分析、失败原因分析等
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("executionId", execution.getId());
            statistics.put("success", result.isSuccess());
            statistics.put("executionTime", result.getExecutionTime());
            statistics.put("timestamp", LocalDateTime.now());
            
            // 可以保存到数据库或发送到监控系统
            logger.info("执行统计分析: {}", statistics);
        } catch (Exception e) {
            logger.error("执行统计分析时出错", e);
        }
    }
    
    /**
     * 发送通知（如果需要）
     */
    private void sendNotificationIfNeeded(TestExecution execution, TestExecutionResult result) {
        try {
            // 这里可以添加通知逻辑
            // 比如：发送邮件、Slack消息、钉钉通知等
            
            if (!result.isSuccess()) {
                logger.warn("测试执行失败，发送通知: {} - {}", execution.getId(), result.getMessage());
                // 发送失败通知
            } else {
                logger.info("测试执行成功: {}", execution.getId());
                // 可以发送成功通知（如果需要）
            }
        } catch (Exception e) {
            logger.error("发送通知时出错", e);
        }
    }
    
    /**
     * 批量处理测试结果
     */
    public void processBatchTestResults(List<TestExecution> executions, List<TestExecutionResult> results) {
        logger.info("批量处理测试结果: {} 个执行", executions.size());
        
        for (int i = 0; i < executions.size() && i < results.size(); i++) {
            processTestResult(executions.get(i), results.get(i));
        }
    }
    
    /**
     * 生成测试执行摘要
     */
    public Map<String, Object> generateExecutionSummary(List<TestExecution> executions) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalTests = executions.size();
        int passedTests = 0;
        int failedTests = 0;
        int skippedTests = 0;
        long totalExecutionTime = 0;
        
        for (TestExecution execution : executions) {
            if (execution.getStatus() == TestExecution.ExecutionStatus.COMPLETED) {
                passedTests++;
            } else if (execution.getStatus() == TestExecution.ExecutionStatus.FAILED) {
                failedTests++;
            }
            
            // 计算执行时间
            if (execution.getStartTime() != null && execution.getEndTime() != null) {
                long duration = java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis();
                totalExecutionTime += duration;
            }
        }
        
        summary.put("totalTests", totalTests);
        summary.put("passedTests", passedTests);
        summary.put("failedTests", failedTests);
        summary.put("skippedTests", skippedTests);
        summary.put("successRate", totalTests > 0 ? (double) passedTests / totalTests * 100 : 0.0);
        summary.put("totalExecutionTime", totalExecutionTime);
        summary.put("averageExecutionTime", totalTests > 0 ? totalExecutionTime / totalTests : 0);
        
        return summary;
    }
}
