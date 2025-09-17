package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestReport;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

@Service
public class TestReportGenerationService {
    
    @Autowired
    private TestReportRepository testReportRepository;
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    public TestReport generateReport(String executionId) {
        Optional<TestExecution> executionOpt = testExecutionRepository.findById(executionId);
        if (!executionOpt.isPresent()) {
            throw new IllegalArgumentException("未找到测试执行记录: " + executionId);
        }
        
        TestExecution execution = executionOpt.get();
        
        // 创建测试报告
        TestReport report = new TestReport();
        report.setId(UUID.randomUUID().toString());
        report.setExecutionId(executionId);
        report.setSuiteId(execution.getSuiteId());
        report.setName("测试执行报告 - " + executionId);
        report.setCreatedAt(LocalDateTime.now());
        
        // 解析执行结果并填充报告摘要和详细信息
        TestReport.ReportSummary summary = new TestReport.ReportSummary();
        List<TestReport.ReportDetail> details = new ArrayList<>();
        
        if (execution.getResult() != null && !execution.getResult().isEmpty()) {
            parseExecutionResult(execution.getResult(), summary, details);
        }
        
        // 设置时间信息
        summary.setStartTime(execution.getStartTime() != null ? execution.getStartTime().toString() : "");
        summary.setEndTime(execution.getEndTime() != null ? execution.getEndTime().toString() : "");
        
        report.setSummary(summary);
        report.setDetails(details);
        
        return testReportRepository.save(report);
    }
    
    private void parseExecutionResult(String result, TestReport.ReportSummary summary, List<TestReport.ReportDetail> details) {
        // 初始化计数器
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        long totalTime = 0;
        
        // 解析结果字符串
        // 假设格式为: "测试用例 '名称': 通过/失败 (耗时: X ms)\n"
        Pattern pattern = Pattern.compile("测试用例 '(.+?)': (通过|失败) \\(耗时: (\\d+) ms\\)");
        Matcher matcher = pattern.matcher(result);
        
        while (matcher.find()) {
            totalTests++;
            
            TestReport.ReportDetail detail = new TestReport.ReportDetail();
            detail.setTestCaseName(matcher.group(1));
            String status = matcher.group(2);
            long responseTime = Long.parseLong(matcher.group(3));
            
            detail.setStatus(status);
            detail.setResponseTime(responseTime);
            
            if ("通过".equals(status)) {
                passedTests++;
            } else {
                failedTests++;
            }
            
            totalTime += responseTime;
            details.add(detail);
        }
        
        // 设置摘要信息
        summary.setTotalTests(totalTests);
        summary.setPassedTests(passedTests);
        summary.setFailedTests(failedTests);
        summary.setSkippedTests(0);
        summary.setPassRate(totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);
        summary.setAverageResponseTime(totalTests > 0 ? totalTime / totalTests : 0);
    }
}