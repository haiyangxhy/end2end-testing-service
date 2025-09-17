package com.testplatform.service;

import com.testplatform.model.TestExecution;

import java.util.List;
import java.util.Optional;

public interface TestExecutionService {
    List<TestExecution> getAllExecutions();
    Optional<TestExecution> getExecutionById(String id);
    List<TestExecution> getExecutionsBySuiteId(String suiteId);
    TestExecution createExecution(TestExecution execution);
    TestExecution updateExecution(String id, TestExecution execution);
    void deleteExecution(String id);
    List<TestExecution> getExecutionsByStatus(TestExecution.ExecutionStatus status);
    
    // 添加执行测试和停止测试的方法
    TestExecution executeTest(String suiteId);
    TestExecution stopExecution(String id);
}