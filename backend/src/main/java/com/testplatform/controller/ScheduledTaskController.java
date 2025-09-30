package com.testplatform.controller;

import com.testplatform.model.ScheduledTask;
import com.testplatform.service.ScheduledTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduled-tasks")
@CrossOrigin(origins = "*")
public class ScheduledTaskController {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskController.class);
    
    @Autowired
    private ScheduledTaskService scheduledTaskService;
    
    /**
     * 获取所有定时任务
     */
    @GetMapping
    public ResponseEntity<List<ScheduledTask>> getAllScheduledTasks() {
        try {
            List<ScheduledTask> tasks = scheduledTaskService.getAllScheduledTasks();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取定时任务列表失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据ID获取定时任务
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduledTask> getScheduledTaskById(@PathVariable String id) {
        try {
            ScheduledTask task = scheduledTaskService.getScheduledTaskById(id);
            return new ResponseEntity<>(task, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("定时任务不存在: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("获取定时任务失败: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 创建定时任务
     */
    @PostMapping
    public ResponseEntity<ScheduledTask> createScheduledTask(@RequestBody ScheduledTask scheduledTask) {
        try {
            logger.info("创建定时任务: {}", scheduledTask.getName());
            ScheduledTask createdTask = scheduledTaskService.createScheduledTask(scheduledTask);
            logger.info("定时任务创建成功: {}", createdTask.getId());
            return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("创建定时任务失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新定时任务
     */
    @PutMapping("/{id}")
    public ResponseEntity<ScheduledTask> updateScheduledTask(@PathVariable String id, @RequestBody ScheduledTask scheduledTask) {
        try {
            logger.info("更新定时任务: {}", id);
            ScheduledTask updatedTask = scheduledTaskService.updateScheduledTask(id, scheduledTask);
            return new ResponseEntity<>(updatedTask, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("定时任务不存在: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("更新定时任务失败: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 删除定时任务
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteScheduledTask(@PathVariable String id) {
        try {
            logger.info("删除定时任务: {}", id);
            scheduledTaskService.deleteScheduledTask(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            logger.warn("删除定时任务失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("删除定时任务时发生未知错误: {}", e.getMessage(), e);
            return new ResponseEntity<>("删除定时任务失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据套件ID获取定时任务
     */
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<ScheduledTask>> getScheduledTasksBySuiteId(@PathVariable String suiteId) {
        try {
            List<ScheduledTask> tasks = scheduledTaskService.getScheduledTasksBySuiteId(suiteId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("根据套件ID获取定时任务失败: {}", suiteId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 根据环境ID获取定时任务
     */
    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<List<ScheduledTask>> getScheduledTasksByEnvironmentId(@PathVariable String environmentId) {
        try {
            List<ScheduledTask> tasks = scheduledTaskService.getScheduledTasksByEnvironmentId(environmentId);
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("根据环境ID获取定时任务失败: {}", environmentId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 启用/禁用定时任务
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<ScheduledTask> toggleScheduledTask(@PathVariable String id, @RequestParam boolean isActive) {
        try {
            logger.info("切换定时任务状态: {} -> {}", id, isActive);
            ScheduledTask task = scheduledTaskService.toggleScheduledTask(id, isActive);
            return new ResponseEntity<>(task, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("定时任务不存在: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("切换定时任务状态失败: {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取需要执行的定时任务
     */
    @GetMapping("/to-execute")
    public ResponseEntity<List<ScheduledTask>> getTasksToExecute() {
        try {
            List<ScheduledTask> tasks = scheduledTaskService.getTasksToExecute();
            return new ResponseEntity<>(tasks, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取需要执行的定时任务失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
