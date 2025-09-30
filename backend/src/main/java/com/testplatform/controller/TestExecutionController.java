package com.testplatform.controller;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestExecutionLog;
import com.testplatform.service.TestExecutionService;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestExecutionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test-executions")
@CrossOrigin(origins = "*")
public class TestExecutionController {
    private static final Logger logger = LoggerFactory.getLogger(TestExecutionController.class);
    
    @Autowired
    private TestExecutionService testExecutionService;
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private TestExecutionLogRepository testExecutionLogRepository;
    
    // 存储活跃的SSE连接
    private final Map<String, SseEmitter> activeConnections = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    /**
     * 执行测试套件
     */
    @PostMapping("/execute")
    public ResponseEntity<TestExecution> executeTestSuite(@RequestBody ExecuteTestSuiteRequest request) {
        try {
            logger.info("开始执行测试套件: {}, 环境: {}", request.getSuiteId(), request.getEnvironmentId());
            
            TestExecution execution = testExecutionService.executeTestSuite(
                request.getSuiteId(), 
                request.getEnvironmentId()
            ).get();
            
            return new ResponseEntity<>(execution, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("执行测试套件失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取测试执行详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<TestExecution> getTestExecution(@PathVariable String id) {
        try {
            TestExecution execution = testExecutionRepository.findById(id).orElse(null);
            if (execution != null) {
                return new ResponseEntity<>(execution, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            logger.error("获取测试执行详情失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取测试执行日志
     */
    @GetMapping("/{id}/logs")
    public ResponseEntity<List<TestExecutionLog>> getExecutionLogs(@PathVariable String id) {
        try {
            List<TestExecutionLog> logs = testExecutionLogRepository.findByExecutionIdOrderByTimestampAsc(id);
            return new ResponseEntity<>(logs, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取测试执行日志失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取实时执行日志（SSE）
     */
    @GetMapping("/{id}/logs/stream")
    public SseEmitter streamExecutionLogs(@PathVariable String id) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        activeConnections.put(id, emitter);
        
        // 设置完成和错误回调
        emitter.onCompletion(() -> {
            activeConnections.remove(id);
            logger.info("SSE连接完成: {}", id);
        });
        
        emitter.onError((throwable) -> {
            activeConnections.remove(id);
            logger.error("SSE连接错误: {}", id, throwable);
        });
        
        emitter.onTimeout(() -> {
            activeConnections.remove(id);
            logger.warn("SSE连接超时: {}", id);
        });
        
        // 启动定时推送
        startLogStreaming(id, emitter);
        
        return emitter;
    }
    
    /**
     * 停止测试执行
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<?> stopTestExecution(@PathVariable String id) {
        try {
            TestExecution execution = testExecutionRepository.findById(id).orElse(null);
            if (execution != null && execution.getStatus() == TestExecution.ExecutionStatus.RUNNING) {
                execution.setStatus(TestExecution.ExecutionStatus.CANCELLED);
                execution.setResult("用户手动停止");
                execution.setEndTime(java.time.LocalDateTime.now());
                testExecutionRepository.save(execution);
                
                logger.info("测试执行已停止: {}", id);
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("停止测试执行失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取所有测试执行记录
     */
    @GetMapping
    public ResponseEntity<List<TestExecution>> getAllTestExecutions() {
        try {
            List<TestExecution> executions = testExecutionRepository.findAll();
            return new ResponseEntity<>(executions, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("获取测试执行记录失败", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 启动日志流推送
     */
    private void startLogStreaming(String executionId, SseEmitter emitter) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // 获取最新的日志
                List<TestExecutionLog> logs = testExecutionLogRepository.findLatestByExecutionId(executionId);
                
                // 发送日志数据
                for (TestExecutionLog log : logs) {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("log")
                            .data(log));
                    } catch (IOException e) {
                        logger.error("发送SSE数据失败", e);
                        activeConnections.remove(executionId);
                        break;
                    }
                }
                
                // 检查执行状态
                TestExecution execution = testExecutionRepository.findById(executionId).orElse(null);
                if (execution != null && (execution.getStatus() == TestExecution.ExecutionStatus.COMPLETED || 
                    execution.getStatus() == TestExecution.ExecutionStatus.FAILED || 
                    execution.getStatus() == TestExecution.ExecutionStatus.CANCELLED)) {
                    
                    // 发送完成事件
                    try {
                        emitter.send(SseEmitter.event()
                            .name("complete")
                            .data(execution));
                        emitter.complete();
                    } catch (IOException e) {
                        logger.error("发送完成事件失败", e);
                    }
                    
                    activeConnections.remove(executionId);
                }
                
            } catch (Exception e) {
                logger.error("日志流推送异常", e);
                activeConnections.remove(executionId);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
    
    /**
     * 执行测试套件请求
     */
    public static class ExecuteTestSuiteRequest {
        private String suiteId;
        private String environmentId;
        
        public String getSuiteId() {
            return suiteId;
        }
        
        public void setSuiteId(String suiteId) {
            this.suiteId = suiteId;
        }
        
        public String getEnvironmentId() {
            return environmentId;
        }
        
        public void setEnvironmentId(String environmentId) {
            this.environmentId = environmentId;
        }
    }
}