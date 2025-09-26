package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestReport;
import com.testplatform.testing.TestExecutionResult;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
        
        if (execution.getExecutionLog() != null && !execution.getExecutionLog().isEmpty()) {
            parseExecutionResult(execution.getExecutionLog(), summary, details);
        }
        
        // 设置时间信息
        summary.setStartTime(execution.getStartTime() != null ? execution.getStartTime().toString() : "");
        summary.setEndTime(execution.getEndTime() != null ? execution.getEndTime().toString() : "");
        
        report.setSummary(summary);
        report.setDetails(details);
        
        return testReportRepository.save(report);
    }
    
    /**
     * 生成综合测试报告
     */
    public TestReport generateComprehensiveReport(String suiteId, LocalDateTime startTime, LocalDateTime endTime) {
        List<TestExecution> executions = testExecutionRepository.findBySuiteId(suiteId);
        
        // 过滤时间范围内的执行
        if (startTime != null && endTime != null) {
            executions = executions.stream()
                    .filter(exec -> exec.getStartTime() != null && 
                            exec.getStartTime().isAfter(startTime) && 
                            exec.getStartTime().isBefore(endTime))
                    .collect(Collectors.toList());
        }
        
        TestReport report = new TestReport();
        report.setId(UUID.randomUUID().toString());
        report.setSuiteId(suiteId);
        report.setName("综合测试报告 - " + suiteId);
        report.setCreatedAt(LocalDateTime.now());
        
        // 生成综合摘要
        TestReport.ReportSummary summary = generateComprehensiveSummary(executions);
        report.setSummary(summary);
        
        // 生成详细报告
        List<TestReport.ReportDetail> details = generateComprehensiveDetails(executions);
        report.setDetails(details);
        
        return testReportRepository.save(report);
    }
    
    /**
     * 生成测试趋势报告
     */
    public Map<String, Object> generateTrendReport(String suiteId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        List<TestExecution> executions = testExecutionRepository.findBySuiteId(suiteId);
        executions = executions.stream()
                .filter(exec -> exec.getStartTime() != null && 
                        exec.getStartTime().isAfter(startTime) && 
                        exec.getStartTime().isBefore(endTime))
                .collect(Collectors.toList());
        
        Map<String, Object> trendReport = new HashMap<>();
        
        // 按日期分组统计
        Map<String, Map<String, Object>> dailyStats = new HashMap<>();
        for (TestExecution execution : executions) {
            String date = execution.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            dailyStats.computeIfAbsent(date, k -> new HashMap<>());
            
            Map<String, Object> dayStats = dailyStats.get(date);
            dayStats.put("total", (Integer) dayStats.getOrDefault("total", 0) + 1);
            
            if (execution.getStatus() == TestExecution.ExecutionStatus.COMPLETED) {
                dayStats.put("passed", (Integer) dayStats.getOrDefault("passed", 0) + 1);
            } else if (execution.getStatus() == TestExecution.ExecutionStatus.FAILED) {
                dayStats.put("failed", (Integer) dayStats.getOrDefault("failed", 0) + 1);
            }
        }
        
        trendReport.put("dailyStats", dailyStats);
        trendReport.put("totalExecutions", executions.size());
        trendReport.put("successRate", calculateSuccessRate(executions));
        trendReport.put("averageExecutionTime", calculateAverageExecutionTime(executions));
        
        return trendReport;
    }
    
    /**
     * 生成性能报告
     */
    public Map<String, Object> generatePerformanceReport(String suiteId, int days) {
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = endTime.minusDays(days);
        
        List<TestExecution> executions = testExecutionRepository.findBySuiteId(suiteId);
        executions = executions.stream()
                .filter(exec -> exec.getStartTime() != null && 
                        exec.getStartTime().isAfter(startTime) && 
                        exec.getStartTime().isBefore(endTime))
                .collect(Collectors.toList());
        
        Map<String, Object> performanceReport = new HashMap<>();
        
        // 计算性能指标
        List<Long> executionTimes = executions.stream()
                .filter(exec -> exec.getStartTime() != null && exec.getEndTime() != null)
                .map(exec -> java.time.Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis())
                .collect(Collectors.toList());
        
        if (!executionTimes.isEmpty()) {
            performanceReport.put("minExecutionTime", executionTimes.stream().mapToLong(Long::longValue).min().orElse(0));
            performanceReport.put("maxExecutionTime", executionTimes.stream().mapToLong(Long::longValue).max().orElse(0));
            performanceReport.put("avgExecutionTime", executionTimes.stream().mapToLong(Long::longValue).average().orElse(0));
            performanceReport.put("totalExecutions", executionTimes.size());
        }
        
        return performanceReport;
    }
    
    private void parseExecutionResult(String result, TestReport.ReportSummary summary, List<TestReport.ReportDetail> details) {
        // 初始化计数器
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        long totalTime = 0;
        
        try {
            // 使用Jackson解析JSON格式的结果
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            
            // 首先尝试解析为JSON对象
            Map<String, Object> resultMap = mapper.readValue(result, Map.class);
            
            // 检查是否包含results字段（新格式）
            if (resultMap.containsKey("results")) {
                List<Map<String, Object>> resultsList = (List<Map<String, Object>>) resultMap.get("results");
                
                for (Map<String, Object> testResult : resultsList) {
                    totalTests++;
                    
                    TestReport.ReportDetail detail = new TestReport.ReportDetail();
                    
                    // 提取基本信息
                    if (testResult.containsKey("testCaseName")) {
                        detail.setTestCaseName((String) testResult.get("testCaseName"));
                    }
                    
                    if (testResult.containsKey("testCaseId")) {
                        detail.setTestCaseId((String) testResult.get("testCaseId"));
                    }
                    
                    if (testResult.containsKey("testType")) {
                        detail.setTestType((String) testResult.get("testType"));
                    }
                    
                    // 设置状态
                    boolean success = testResult.containsKey("success") && (boolean) testResult.get("success");
                    detail.setStatus(success ? "通过" : "失败");
                    
                    // 设置执行时间
                    if (testResult.containsKey("executionTime")) {
                        long executionTime = ((Number) testResult.get("executionTime")).longValue();
                        detail.setResponseTime(executionTime);
                        totalTime += executionTime;
                    }
                    
                    // 设置消息
                    if (testResult.containsKey("message")) {
                        detail.setMessage((String) testResult.get("message"));
                    }
                    
                    // 设置错误信息
                    if (testResult.containsKey("errorMessage")) {
                        detail.setErrorMessage((String) testResult.get("errorMessage"));
                    }
                    
                    if (testResult.containsKey("errorDetails")) {
                        detail.setErrorDetails((String) testResult.get("errorDetails"));
                    }
                    
                    // 设置时间戳
                    if (testResult.containsKey("timestamp")) {
                        detail.setTimestamp((String) testResult.get("timestamp"));
                    }
                    
                    // 设置元数据
                    if (testResult.containsKey("metadata")) {
                        Map<String, Object> metadata = (Map<String, Object>) testResult.get("metadata");
                        if (metadata != null) {
                            // 转换元数据为字符串
                            String metadataStr = mapper.writeValueAsString(metadata);
                            detail.setMetadata(metadataStr);
                        }
                    }
                    
                    if (success) {
                        passedTests++;
                    } else {
                        failedTests++;
                    }
                    
                    details.add(detail);
                }
            } else {
                // 兼容旧格式
                parseLegacyExecutionResult(result, summary, details);
                return;
            }
        } catch (Exception e) {
            // 如果JSON解析失败，尝试使用旧的文本解析方式
            parseLegacyExecutionResult(result, summary, details);
            return;
        }
        
        // 设置摘要信息
        summary.setTotalTests(totalTests);
        summary.setPassedTests(passedTests);
        summary.setFailedTests(failedTests);
        summary.setSkippedTests(0);
        summary.setPassRate(totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);
        summary.setAverageResponseTime(totalTests > 0 ? totalTime / totalTests : 0);
    }
    
    private void parseLegacyExecutionResult(String result, TestReport.ReportSummary summary, List<TestReport.ReportDetail> details) {
        // 初始化计数器
        int totalTests = 0;
        int passedTests = 0;
        int failedTests = 0;
        long totalTime = 0;
        
        // 解析结果字符串
        // 假设格式为: "测试用例 '名称': 通过/失败 (耗时: X ms)\n"
        Pattern pattern = Pattern.compile("测试用例 '(\\w+(?:\\s*\\w*)*)': (通过|失败) \\(耗时: (\\d+) ms\\)");
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
    
    /**
     * 生成综合摘要
     */
    private TestReport.ReportSummary generateComprehensiveSummary(List<TestExecution> executions) {
        TestReport.ReportSummary summary = new TestReport.ReportSummary();
        
        int totalTests = executions.size();
        int passedTests = 0;
        int failedTests = 0;
        long totalTime = 0;
        
        for (TestExecution execution : executions) {
            if (execution.getStatus() == TestExecution.ExecutionStatus.COMPLETED) {
                passedTests++;
            } else if (execution.getStatus() == TestExecution.ExecutionStatus.FAILED) {
                failedTests++;
            }
            
            if (execution.getStartTime() != null && execution.getEndTime() != null) {
                long duration = java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis();
                totalTime += duration;
            }
        }
        
        summary.setTotalTests(totalTests);
        summary.setPassedTests(passedTests);
        summary.setFailedTests(failedTests);
        summary.setSkippedTests(0);
        summary.setPassRate(totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);
        summary.setAverageResponseTime(totalTests > 0 ? totalTime / totalTests : 0);
        
        if (!executions.isEmpty()) {
            summary.setStartTime(executions.stream()
                    .filter(exec -> exec.getStartTime() != null)
                    .map(exec -> exec.getStartTime().toString())
                    .min(String::compareTo)
                    .orElse(""));
            
            summary.setEndTime(executions.stream()
                    .filter(exec -> exec.getEndTime() != null)
                    .map(exec -> exec.getEndTime().toString())
                    .max(String::compareTo)
                    .orElse(""));
        }
        
        return summary;
    }
    
    /**
     * 生成综合详细报告
     */
    private List<TestReport.ReportDetail> generateComprehensiveDetails(List<TestExecution> executions) {
        List<TestReport.ReportDetail> details = new ArrayList<>();
        
        for (TestExecution execution : executions) {
            TestReport.ReportDetail detail = new TestReport.ReportDetail();
            detail.setTestCaseId(execution.getId());
            detail.setTestCaseName("测试执行 - " + execution.getId());
            detail.setStatus(execution.getStatus() == TestExecution.ExecutionStatus.COMPLETED ? "PASSED" : "FAILED");
            detail.setMessage(execution.getExecutionLog());
            detail.setTimestamp(execution.getStartTime() != null ? execution.getStartTime().toString() : "");
            
            if (execution.getStartTime() != null && execution.getEndTime() != null) {
                long duration = java.time.Duration.between(execution.getStartTime(), execution.getEndTime()).toMillis();
                detail.setResponseTime(duration);
            }
            
            details.add(detail);
        }
        
        return details;
    }
    
    /**
     * 计算成功率
     */
    private double calculateSuccessRate(List<TestExecution> executions) {
        if (executions.isEmpty()) {
            return 0.0;
        }
        
        long successCount = executions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED)
                .count();
        
        return (double) successCount / executions.size() * 100;
    }
    
    /**
     * 计算平均执行时间
     */
    private double calculateAverageExecutionTime(List<TestExecution> executions) {
        List<Long> executionTimes = executions.stream()
                .filter(exec -> exec.getStartTime() != null && exec.getEndTime() != null)
                .map(exec -> java.time.Duration.between(exec.getStartTime(), exec.getEndTime()).toMillis())
                .collect(Collectors.toList());
        
        if (executionTimes.isEmpty()) {
            return 0.0;
        }
        
        return executionTimes.stream().mapToLong(Long::longValue).average().orElse(0.0);
    }
    
    /**
     * 导出报告为JSON
     */
    public String exportReportAsJson(String reportId) {
        Optional<TestReport> reportOpt = testReportRepository.findById(reportId);
        if (!reportOpt.isPresent()) {
            throw new IllegalArgumentException("未找到测试报告: " + reportId);
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(reportOpt.get());
        } catch (Exception e) {
            throw new RuntimeException("导出报告失败", e);
        }
    }
    
    /**
     * 导出报告为CSV
     */
    public String exportReportAsCsv(String reportId) {
        Optional<TestReport> reportOpt = testReportRepository.findById(reportId);
        if (!reportOpt.isPresent()) {
            throw new IllegalArgumentException("未找到测试报告: " + reportId);
        }
        
        TestReport report = reportOpt.get();
        StringBuilder csv = new StringBuilder();
        
        // CSV头部
        csv.append("测试用例ID,测试用例名称,状态,执行时间(ms),消息,时间戳\n");
        
        // 数据行
        if (report.getDetails() != null) {
            for (TestReport.ReportDetail detail : report.getDetails()) {
                csv.append(detail.getTestCaseId()).append(",");
                csv.append(detail.getTestCaseName()).append(",");
                csv.append(detail.getStatus()).append(",");
                csv.append(detail.getResponseTime()).append(",");
                csv.append("\"").append(detail.getMessage() != null ? detail.getMessage().replace("\"", "\"\"") : "").append("\",");
                csv.append(detail.getTimestamp()).append("\n");
            }
        }
        
        return csv.toString();
    }
}