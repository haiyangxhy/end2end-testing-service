package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.repository.TestExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 告警服务
 * 负责监控测试执行状态，发送告警通知
 */
@Service
public class AlertService {
    private static final Logger logger = LoggerFactory.getLogger(AlertService.class);
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private RealTimeMonitoringService realTimeMonitoringService;
    
    // 告警规则配置
    private final Map<String, AlertRule> alertRules = new ConcurrentHashMap<>();
    
    // 告警历史记录
    private final List<AlertRecord> alertHistory = new ArrayList<>();
    
    public AlertService() {
        initializeDefaultRules();
    }
    
    /**
     * 初始化默认告警规则
     */
    private void initializeDefaultRules() {
        // 测试失败率告警
        alertRules.put("high_failure_rate", new AlertRule(
                "high_failure_rate",
                "测试失败率过高",
                "当测试失败率超过50%时触发告警",
                AlertRule.AlertType.METRIC,
                "failure_rate > 0.5",
                300 // 5分钟检查间隔
        ));
        
        // 长时间运行测试告警
        alertRules.put("long_running_test", new AlertRule(
                "long_running_test",
                "测试执行时间过长",
                "当测试执行时间超过30分钟时触发告警",
                AlertRule.AlertType.DURATION,
                "execution_time > 1800000", // 30分钟
                60 // 1分钟检查间隔
        ));
        
        // 连续失败告警
        alertRules.put("consecutive_failures", new AlertRule(
                "consecutive_failures",
                "连续测试失败",
                "当连续3次测试失败时触发告警",
                AlertRule.AlertType.CONSECUTIVE,
                "consecutive_failures >= 3",
                120 // 2分钟检查间隔
        ));
    }
    
    /**
     * 检查告警条件
     */
    public void checkAlerts() {
        logger.debug("开始检查告警条件");
        
        for (AlertRule rule : alertRules.values()) {
            try {
                if (shouldCheckRule(rule)) {
                    checkRule(rule);
                }
            } catch (Exception e) {
                logger.error("检查告警规则失败: {}", rule.getName(), e);
            }
        }
    }
    
    /**
     * 判断是否应该检查规则
     */
    private boolean shouldCheckRule(AlertRule rule) {
        AlertRecord lastAlert = getLastAlert(rule.getName());
        if (lastAlert == null) {
            return true;
        }
        
        return LocalDateTime.now().isAfter(lastAlert.getLastChecked().plusSeconds(rule.getCheckInterval()));
    }
    
    /**
     * 检查具体规则
     */
    private void checkRule(AlertRule rule) {
        boolean triggered = false;
        String message = "";
        
        switch (rule.getType()) {
            case METRIC:
                triggered = checkMetricRule(rule);
                break;
            case DURATION:
                triggered = checkDurationRule(rule);
                break;
            case CONSECUTIVE:
                triggered = checkConsecutiveRule(rule);
                break;
        }
        
        if (triggered) {
            message = generateAlertMessage(rule);
            sendAlert(rule, message);
        }
        
        // 更新检查时间
        updateLastChecked(rule.getName());
    }
    
    /**
     * 检查指标规则
     */
    private boolean checkMetricRule(AlertRule rule) {
        if ("high_failure_rate".equals(rule.getName())) {
            return checkHighFailureRate();
        }
        return false;
    }
    
    /**
     * 检查失败率
     */
    private boolean checkHighFailureRate() {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<TestExecution> recentExecutions = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStartTime() != null && exec.getStartTime().isAfter(oneHourAgo))
                .collect(Collectors.toList());
        
        if (recentExecutions.isEmpty()) {
            return false;
        }
        
        long failedCount = recentExecutions.stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.FAILED)
                .count();
        
        double failureRate = (double) failedCount / recentExecutions.size();
        return failureRate > 0.5;
    }
    
    /**
     * 检查持续时间规则
     */
    private boolean checkDurationRule(AlertRule rule) {
        if ("long_running_test".equals(rule.getName())) {
            return checkLongRunningTests();
        }
        return false;
    }
    
    /**
     * 检查长时间运行的测试
     */
    private boolean checkLongRunningTests() {
        List<TestExecution> runningTests = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStatus() == TestExecution.ExecutionStatus.RUNNING)
                .collect(Collectors.toList());
        
        for (TestExecution test : runningTests) {
            if (test.getStartTime() != null) {
                long duration = java.time.Duration.between(test.getStartTime(), LocalDateTime.now()).toMillis();
                if (duration > 1800000) { // 30分钟
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 检查连续失败规则
     */
    private boolean checkConsecutiveRule(AlertRule rule) {
        if ("consecutive_failures".equals(rule.getName())) {
            return checkConsecutiveFailures();
        }
        return false;
    }
    
    /**
     * 检查连续失败
     */
    private boolean checkConsecutiveFailures() {
        List<TestExecution> recentExecutions = testExecutionRepository.findAll().stream()
                .filter(exec -> exec.getStartTime() != null && 
                        exec.getStartTime().isAfter(LocalDateTime.now().minusHours(2)))
                .sorted((a, b) -> b.getStartTime().compareTo(a.getStartTime()))
                .collect(Collectors.toList());
        
        int consecutiveFailures = 0;
        for (TestExecution exec : recentExecutions) {
            if (exec.getStatus() == TestExecution.ExecutionStatus.FAILED) {
                consecutiveFailures++;
            } else if (exec.getStatus() == TestExecution.ExecutionStatus.COMPLETED) {
                break;
            }
        }
        
        return consecutiveFailures >= 3;
    }
    
    /**
     * 生成告警消息
     */
    private String generateAlertMessage(AlertRule rule) {
        switch (rule.getName()) {
            case "high_failure_rate":
                return "警告：过去1小时内测试失败率超过50%";
            case "long_running_test":
                return "警告：有测试执行时间超过30分钟";
            case "consecutive_failures":
                return "警告：连续3次测试失败";
            default:
                return "警告：" + rule.getDescription();
        }
    }
    
    /**
     * 发送告警
     */
    private void sendAlert(AlertRule rule, String message) {
        AlertRecord alert = new AlertRecord(
                rule.getName(),
                message,
                LocalDateTime.now(),
                AlertRecord.AlertLevel.WARNING
        );
        
        alertHistory.add(alert);
        
        // 记录日志
        logger.warn("告警触发: {} - {}", rule.getName(), message);
        
        // 这里可以添加其他通知方式，如邮件、短信、钉钉等
        // sendEmailNotification(alert);
        // sendSmsNotification(alert);
        // sendDingTalkNotification(alert);
    }
    
    /**
     * 获取告警历史
     */
    public List<AlertRecord> getAlertHistory() {
        return new ArrayList<>(alertHistory);
    }
    
    /**
     * 获取告警规则
     */
    public Map<String, AlertRule> getAlertRules() {
        return new ConcurrentHashMap<>(alertRules);
    }
    
    /**
     * 添加告警规则
     */
    public void addAlertRule(AlertRule rule) {
        alertRules.put(rule.getName(), rule);
        logger.info("添加告警规则: {}", rule.getName());
    }
    
    /**
     * 删除告警规则
     */
    public void removeAlertRule(String ruleName) {
        alertRules.remove(ruleName);
        logger.info("删除告警规则: {}", ruleName);
    }
    
    /**
     * 获取最后告警记录
     */
    private AlertRecord getLastAlert(String ruleName) {
        return alertHistory.stream()
                .filter(alert -> ruleName.equals(alert.getRuleName()))
                .max((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                .orElse(null);
    }
    
    /**
     * 更新最后检查时间
     */
    private void updateLastChecked(String ruleName) {
        AlertRecord lastAlert = getLastAlert(ruleName);
        if (lastAlert != null) {
            lastAlert.setLastChecked(LocalDateTime.now());
        }
    }
    
    /**
     * 告警规则类
     */
    public static class AlertRule {
        private String name;
        private String displayName;
        private String description;
        private AlertType type;
        private String condition;
        private int checkInterval; // 秒
        
        public AlertRule(String name, String displayName, String description, 
                        AlertType type, String condition, int checkInterval) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.type = type;
            this.condition = condition;
            this.checkInterval = checkInterval;
        }
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public AlertType getType() { return type; }
        public void setType(AlertType type) { this.type = type; }
        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }
        public int getCheckInterval() { return checkInterval; }
        public void setCheckInterval(int checkInterval) { this.checkInterval = checkInterval; }
        
        public enum AlertType {
            METRIC, DURATION, CONSECUTIVE
        }
    }
    
    /**
     * 告警记录类
     */
    public static class AlertRecord {
        private String ruleName;
        private String message;
        private LocalDateTime timestamp;
        private AlertLevel level;
        private LocalDateTime lastChecked;
        
        public AlertRecord(String ruleName, String message, LocalDateTime timestamp, AlertLevel level) {
            this.ruleName = ruleName;
            this.message = message;
            this.timestamp = timestamp;
            this.level = level;
            this.lastChecked = timestamp;
        }
        
        // Getters and Setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
        public AlertLevel getLevel() { return level; }
        public void setLevel(AlertLevel level) { this.level = level; }
        public LocalDateTime getLastChecked() { return lastChecked; }
        public void setLastChecked(LocalDateTime lastChecked) { this.lastChecked = lastChecked; }
        
        public enum AlertLevel {
            INFO, WARNING, ERROR, CRITICAL
        }
    }
}
