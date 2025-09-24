package com.testplatform.controller;

import com.testplatform.service.RealTimeMonitoringService;
import com.testplatform.service.TestReportGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 监控控制器
 * 提供实时监控和报告相关的API
 */
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {
    
    @Autowired
    private RealTimeMonitoringService realTimeMonitoringService;
    
    @Autowired
    private TestReportGenerationService testReportGenerationService;
    
    /**
     * 建立SSE连接
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        String clientId = UUID.randomUUID().toString();
        return realTimeMonitoringService.addConnection(clientId);
    }
    
    /**
     * 获取实时数据
     */
    @GetMapping("/realtime")
    public ResponseEntity<Map<String, Object>> getRealTimeData() {
        try {
            // 这里需要访问私有方法，我们创建一个公共方法
            Map<String, Object> data = realTimeMonitoringService.getRealTimeData();
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 获取统计报告
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> statistics = realTimeMonitoringService.getStatisticsReport();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 生成综合测试报告
     */
    @PostMapping("/reports/comprehensive")
    public ResponseEntity<Map<String, Object>> generateComprehensiveReport(
            @RequestParam String suiteId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            var report = testReportGenerationService.generateComprehensiveReport(suiteId, start, end);
            
            return ResponseEntity.ok(Map.of(
                    "reportId", report.getId(),
                    "suiteId", report.getSuiteId(),
                    "name", report.getName(),
                    "createdAt", report.getCreatedAt().toString(),
                    "summary", report.getSummary()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 生成趋势报告
     */
    @GetMapping("/reports/trend")
    public ResponseEntity<Map<String, Object>> generateTrendReport(
            @RequestParam String suiteId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> trendReport = testReportGenerationService.generateTrendReport(suiteId, days);
            return ResponseEntity.ok(trendReport);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 生成性能报告
     */
    @GetMapping("/reports/performance")
    public ResponseEntity<Map<String, Object>> generatePerformanceReport(
            @RequestParam String suiteId,
            @RequestParam(defaultValue = "7") int days) {
        try {
            Map<String, Object> performanceReport = testReportGenerationService.generatePerformanceReport(suiteId, days);
            return ResponseEntity.ok(performanceReport);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 导出报告为JSON
     */
    @GetMapping("/reports/{reportId}/export/json")
    public ResponseEntity<String> exportReportAsJson(@PathVariable String reportId) {
        try {
            String jsonReport = testReportGenerationService.exportReportAsJson(reportId);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jsonReport);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 导出报告为CSV
     */
    @GetMapping("/reports/{reportId}/export/csv")
    public ResponseEntity<String> exportReportAsCsv(@PathVariable String reportId) {
        try {
            String csvReport = testReportGenerationService.exportReportAsCsv(reportId);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .header("Content-Disposition", "attachment; filename=test-report-" + reportId + ".csv")
                    .body(csvReport);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
    
    /**
     * 获取活跃连接数
     */
    @GetMapping("/connections/count")
    public ResponseEntity<Map<String, Object>> getConnectionCount() {
        int count = realTimeMonitoringService.getActiveConnectionCount();
        return ResponseEntity.ok(Map.of("activeConnections", count));
    }
    
    /**
     * 关闭所有连接
     */
    @PostMapping("/connections/close-all")
    public ResponseEntity<Map<String, Object>> closeAllConnections() {
        realTimeMonitoringService.closeAllConnections();
        return ResponseEntity.ok(Map.of("message", "所有连接已关闭"));
    }
}
