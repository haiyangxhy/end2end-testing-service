package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestSuite;
import com.testplatform.model.TestEnvironment;

import java.util.concurrent.CompletableFuture;

public interface TestExecutionService {
    
    /**
     * 执行测试套件
     * @param suiteId 测试套件ID
     * @param environmentId 环境ID
     * @return 测试执行结果
     */
    CompletableFuture<TestExecution> executeTestSuite(String suiteId, String environmentId);
    
    /**
     * 开始执行测试
     * @param executionId 执行ID
     */
    void startExecution(String executionId);
    
    /**
     * 停止执行测试
     * @param executionId 执行ID
     */
    void stopExecution(String executionId);
    
    /**
     * 获取执行状态
     * @param executionId 执行ID
     * @return 执行状态
     */
    TestExecution getExecutionStatus(String executionId);
}