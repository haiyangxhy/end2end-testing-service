package com.testplatform.service;

import com.testplatform.model.ScheduledTask;
import java.util.List;

public interface ScheduledTaskService {
    
    /**
     * 获取所有定时任务
     */
    List<ScheduledTask> getAllScheduledTasks();
    
    /**
     * 根据ID获取定时任务
     */
    ScheduledTask getScheduledTaskById(String id);
    
    /**
     * 创建定时任务
     */
    ScheduledTask createScheduledTask(ScheduledTask scheduledTask);
    
    /**
     * 更新定时任务
     */
    ScheduledTask updateScheduledTask(String id, ScheduledTask scheduledTask);
    
    /**
     * 删除定时任务
     */
    void deleteScheduledTask(String id);
    
    /**
     * 根据套件ID获取定时任务
     */
    List<ScheduledTask> getScheduledTasksBySuiteId(String suiteId);
    
    /**
     * 根据环境ID获取定时任务
     */
    List<ScheduledTask> getScheduledTasksByEnvironmentId(String environmentId);
    
    /**
     * 启用/禁用定时任务
     */
    ScheduledTask toggleScheduledTask(String id, boolean isActive);
    
    /**
     * 获取需要执行的定时任务
     */
    List<ScheduledTask> getTasksToExecute();
    
    /**
     * 更新任务执行时间
     */
    void updateTaskExecutionTime(String id, java.time.LocalDateTime lastRun, java.time.LocalDateTime nextRun);
}
