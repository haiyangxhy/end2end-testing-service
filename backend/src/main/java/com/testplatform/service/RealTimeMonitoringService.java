package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestReport;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 实时监控服务
 * 负责监控测试执行状态，提供实时数据推送
 */
@Service
public class RealTimeMonitoringService {
    private static final Logger logger = LoggerFactory.getLogger(RealTimeMonitoringService.class);
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private TestReportRepository testReportRepository;
    
    // 存储活跃的SSE连接
    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();
    
    // 定时任务执行器
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public RealTimeMonitoringService() {
        // 启动定时监控任务
        startMonitoringTasks();
    }
    
    /**
     * 添加SSE连接
     */
    public SseEmitter addConnection(String clientId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        activeConnections.put(clientId, emitter);
        
        emitter.onCompletion(() -> {
            activeConnections.remove(clientId);
            logger.info("SSE连接完成: {}", clientId);
        });
        
        emitter.onTimeout(() -> {
            activeConnections.remove(clientId);
            logger.info("SSE连接超时: {}", clientId);
        });
        
        emitter.onError((ex) -> {
            activeConnections.remove(clientId);
            logger.error("SSE连接错误: {}", clientId, ex);
        });
        
        logger.info("添加SSE连接: {}", clientId);
        return emitter;
    }
    
    /**
     * 移除SSE连接
     */
    public void removeConnection(String clientId) {
        SseEmitter emitter = activeConnections.remove(clientId);
        if (emitter != null) {
            emitter.complete();
            logger.info("移除SSE连接: {}", clientId);
        }
    }
    
    /**
     * 启动监控任务
     */
    private void startMonitoringTasks() {
        // 每5秒推送一次实时数据
        scheduler.scheduleAtFixedRate(this::pushRealTimeData, 0, 5, TimeUnit.SECONDS);
        
        // 每30秒推送一次统计报告
        scheduler.scheduleAtFixedRate(this::pushStatisticsReport, 0, 30, TimeUnit.SECONDS);
    }
    
    /**
     * 推送实时数据
     */
    private void pushRealTimeData() {
        if (activeConnections.isEmpty()) {
            return;
        }
        
        try {
            Map<String, Object> realTimeData = getRealTimeData();
            
            for (Map.Entry<String, SseEmitter> entry : activeConnections.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                            .name("realTimeData")
                            .data(realTimeData));
                } catch (Exception e) {
                    logger.error("推送实时数据失败: {}", entry.getKey(), e);
                    activeConnections.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            logger.error("获取实时数据失败", e);
        }
    }
    
    /**
     * 推送统计报告
     */
    private void pushStatisticsReport() {
        if (activeConnections.isEmpty()) {
            return;
        }
        
        try {
            Map<String, Object> statisticsReport = getStatisticsReport();
            
            for (Map.Entry<String, SseEmitter> entry : activeConnections.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                            .name("statisticsReport")
                            .data(statisticsReport));
                } catch (Exception e) {
                    logger.error("推送统计报告失败: {}", entry.getKey(), e);
                    activeConnections.remove(entry.getKey());
                }
            }
        } catch (Exception e) {
            logger.error("获取统计报告失败", e);
        }
    }
    
    /**
     * 获取实时数据
     */
    public Map<String, Object> getRealTimeData() {
        Map<String, Object> data = new ConcurrentHashMap<>();
        
        // 获取当前正在执行的测试
        List<TestExecution> runningTests = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.RUNNING)
                .collect(Collectors.toList());
        
        data.put("runningTests", runningTests.size());
        data.put("runningTestDetails", runningTests.stream()
                .map(exec -> Map.of(
                        "id", exec.getId(),
                        "suiteId", exec.getSuiteId(),
                        "startTime", exec.getStartTime() != null ? exec.getStartTime().toString() : "",
                        "duration", exec.getStartTime() != null ? 
                                java.time.Duration.between(exec.getStartTime(), LocalDateTime.now()).toMillis() : 0
                ))
                .collect(Collectors.toList()));
        
        // 获取最近完成的测试
        List<TestExecution> recentCompleted = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED)
                .filter(exec -> exec.getEndTime() != null && 
                        exec.getEndTime().isAfter(LocalDateTime.now().minusMinutes(10)))
                .collect(Collectors.toList());
        
        data.put("recentCompleted", recentCompleted.size());
        
        // 获取最近失败的测试
        List<TestExecution> recentFailed = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .filter(exec -> exec.getEndTime() != null && 
                        exec.getEndTime().isAfter(LocalDateTime.now().minusMinutes(10)))
                .collect(Collectors.toList());
        
        data.put("recentFailed", recentFailed.size());
        
        data.put("timestamp", LocalDateTime.now().toString());
        
        return data;
    }
    
    /**
     * 获取统计报告
     */
    public Map<String, Object> getStatisticsReport() {
        Map<String, Object> report = new ConcurrentHashMap<>();
        
        List<TestExecution> allExecutions = testExecutionRepository.findAll();
        
        // 今日统计
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        List<TestExecution> todayExecutions = allExecutions.stream()
                .filter(exec -> exec.getStartTime() != null && exec.getStartTime().isAfter(todayStart))
                .collect(Collectors.toList());
        
        long todayTotal = todayExecutions.size();
        long todayPassed = todayExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED)
                .count();
        long todayFailed = todayExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .count();
        
        report.put("today", Map.of(
                "total", todayTotal,
                "passed", todayPassed,
                "failed", todayFailed,
                "successRate", todayTotal > 0 ? (double) todayPassed / todayTotal * 100 : 0.0
        ));
        
        // 本周统计
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<TestExecution> weekExecutions = allExecutions.stream()
                .filter(exec -> exec.getStartTime() != null && exec.getStartTime().isAfter(weekStart))
                .collect(Collectors.toList());
        
        long weekTotal = weekExecutions.size();
        long weekPassed = weekExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED)
                .count();
        long weekFailed = weekExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .count();
        
        report.put("week", Map.of(
                "total", weekTotal,
                "passed", weekPassed,
                "failed", weekFailed,
                "successRate", weekTotal > 0 ? (double) weekPassed / weekTotal * 100 : 0.0
        ));
        
        // 总体统计
        long totalExecutions = allExecutions.size();
        long totalPassed = allExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED)
                .count();
        long totalFailed = allExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .count();
        
        report.put("overall", Map.of(
                "total", totalExecutions,
                "passed", totalPassed,
                "failed", totalFailed,
                "successRate", totalExecutions > 0 ? (double) totalPassed / totalExecutions * 100 : 0.0
        ));
        
        report.put("timestamp", LocalDateTime.now().toString());
        
        return report;
    }
    
    /**
     * 推送测试执行状态更新
     */
    public void pushTestExecutionUpdate(TestExecution execution) {
        if (activeConnections.isEmpty()) {
            return;
        }
        
        Map<String, Object> updateData = Map.of(
                "type", "testExecutionUpdate",
                "executionId", execution.getId(),
                "status", execution.getStatus().toString(),
                "timestamp", LocalDateTime.now().toString()
        );
        
        for (Map.Entry<String, SseEmitter> entry : activeConnections.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("testExecutionUpdate")
                        .data(updateData));
            } catch (Exception e) {
                logger.error("推送测试执行更新失败: {}", entry.getKey(), e);
                activeConnections.remove(entry.getKey());
            }
        }
    }
    
    /**
     * 推送测试报告更新
     */
    public void pushTestReportUpdate(TestReport report) {
        if (activeConnections.isEmpty()) {
            return;
        }
        
        Map<String, Object> updateData = Map.of(
                "type", "testReportUpdate",
                "reportId", report.getId(),
                "suiteId", report.getSuiteId(),
                "timestamp", LocalDateTime.now().toString()
        );
        
        for (Map.Entry<String, SseEmitter> entry : activeConnections.entrySet()) {
            try {
                entry.getValue().send(SseEmitter.event()
                        .name("testReportUpdate")
                        .data(updateData));
            } catch (Exception e) {
                logger.error("推送测试报告更新失败: {}", entry.getKey(), e);
                activeConnections.remove(entry.getKey());
            }
        }
    }
    
    /**
     * 获取活跃连接数
     */
    public int getActiveConnectionCount() {
        return activeConnections.size();
    }
    
    /**
     * 关闭所有连接
     */
    public void closeAllConnections() {
        for (Map.Entry<String, SseEmitter> entry : activeConnections.entrySet()) {
            try {
                entry.getValue().complete();
            } catch (Exception e) {
                logger.error("关闭连接失败: {}", entry.getKey(), e);
            }
        }
        activeConnections.clear();
        logger.info("已关闭所有SSE连接");
    }
}
