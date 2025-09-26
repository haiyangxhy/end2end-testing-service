package com.testplatform.service;

import com.testplatform.model.ScheduledTask;
import com.testplatform.repository.ScheduledTaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ScheduledTaskServiceImpl implements ScheduledTaskService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskServiceImpl.class);
    
    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;
    
    @Override
    public List<ScheduledTask> getAllScheduledTasks() {
        return scheduledTaskRepository.findAll();
    }
    
    @Override
    public ScheduledTask getScheduledTaskById(String id) {
        Optional<ScheduledTask> task = scheduledTaskRepository.findById(id);
        if (task.isPresent()) {
            return task.get();
        } else {
            throw new RuntimeException("定时任务不存在: " + id);
        }
    }
    
    @Override
    public ScheduledTask createScheduledTask(ScheduledTask scheduledTask) {
        logger.info("创建定时任务: {}", scheduledTask.getName());
        
        // 设置创建者
        String currentUserId = getCurrentUserId();
        scheduledTask.setCreatedBy(currentUserId);
        
        // 设置时间戳
        scheduledTask.setCreatedAt(LocalDateTime.now());
        scheduledTask.setUpdatedAt(LocalDateTime.now());
        
        // 计算下次执行时间
        calculateNextRun(scheduledTask);
        
        return scheduledTaskRepository.save(scheduledTask);
    }
    
    @Override
    public ScheduledTask updateScheduledTask(String id, ScheduledTask scheduledTask) {
        logger.info("更新定时任务: {}", id);
        
        // 先获取现有的定时任务以保留created_at和created_by
        Optional<ScheduledTask> existingTaskOpt = scheduledTaskRepository.findById(id);
        if (!existingTaskOpt.isPresent()) {
            throw new RuntimeException("定时任务不存在: " + id);
        }
        
        ScheduledTask existingTask = existingTaskOpt.get();
        
        // 保留原有的created_at和created_by
        scheduledTask.setId(id);
        scheduledTask.setCreatedAt(existingTask.getCreatedAt());
        scheduledTask.setCreatedBy(existingTask.getCreatedBy());
        scheduledTask.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算下次执行时间
        calculateNextRun(scheduledTask);
        
        return scheduledTaskRepository.save(scheduledTask);
    }
    
    @Override
    @Transactional
    public void deleteScheduledTask(String id) {
        logger.info("删除定时任务: {}", id);
        
        if (!scheduledTaskRepository.existsById(id)) {
            throw new RuntimeException("定时任务不存在: " + id);
        }
        
        scheduledTaskRepository.deleteById(id);
        logger.info("定时任务删除完成: {}", id);
    }
    
    @Override
    public List<ScheduledTask> getScheduledTasksBySuiteId(String suiteId) {
        return scheduledTaskRepository.findBySuiteId(suiteId);
    }
    
    @Override
    public List<ScheduledTask> getScheduledTasksByEnvironmentId(String environmentId) {
        return scheduledTaskRepository.findByEnvironmentId(environmentId);
    }
    
    @Override
    public ScheduledTask toggleScheduledTask(String id, boolean isActive) {
        logger.info("切换定时任务状态: {} -> {}", id, isActive);
        
        Optional<ScheduledTask> taskOpt = scheduledTaskRepository.findById(id);
        if (!taskOpt.isPresent()) {
            throw new RuntimeException("定时任务不存在: " + id);
        }
        
        ScheduledTask task = taskOpt.get();
        task.setIsActive(isActive);
        task.setUpdatedAt(LocalDateTime.now());
        
        // 如果启用任务，重新计算下次执行时间
        if (isActive) {
            calculateNextRun(task);
        }
        
        return scheduledTaskRepository.save(task);
    }
    
    @Override
    public List<ScheduledTask> getTasksToExecute() {
        return scheduledTaskRepository.findTasksToExecute(LocalDateTime.now());
    }
    
    @Override
    public void updateTaskExecutionTime(String id, LocalDateTime lastRun, LocalDateTime nextRun) {
        logger.info("更新任务执行时间: {} - 最后执行: {}, 下次执行: {}", id, lastRun, nextRun);
        
        if (lastRun != null) {
            scheduledTaskRepository.updateLastRun(id, lastRun);
        }
        if (nextRun != null) {
            scheduledTaskRepository.updateNextRun(id, nextRun);
        }
    }
    
    /**
     * 计算下次执行时间
     */
    private void calculateNextRun(ScheduledTask task) {
        LocalDateTime now = LocalDateTime.now();
        String cronExpression = task.getCronExpression();
        
        // 简单的cron表达式解析（可以根据需要扩展）
        if (cronExpression.equals("0 0 2 * * ?")) {
            // 每天凌晨2点
            task.setNextRun(now.plusDays(1).withHour(2).withMinute(0).withSecond(0));
        } else if (cronExpression.equals("0 0 */6 * * ?")) {
            // 每6小时
            task.setNextRun(now.plusHours(6));
        } else if (cronExpression.equals("0 0 0 * * ?")) {
            // 每天午夜
            task.setNextRun(now.plusDays(1).withHour(0).withMinute(0).withSecond(0));
        } else if (cronExpression.equals("0 0 */1 * * ?")) {
            // 每小时
            task.setNextRun(now.plusHours(1));
        } else {
            // 默认1小时后
            task.setNextRun(now.plusHours(1));
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String username = authentication.getName();
                return mapUsernameToUserId(username);
            }
        } catch (Exception e) {
            logger.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return "system";
    }
    
    /**
     * 将用户名映射到用户ID
     */
    private String mapUsernameToUserId(String username) {
        // 这里可以根据实际需求实现用户ID映射逻辑
        // 目前简单返回用户名
        return username;
    }
}
