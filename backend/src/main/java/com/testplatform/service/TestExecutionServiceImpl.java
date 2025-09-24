package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestCase;
import com.testplatform.model.TestEnvironment;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestCaseRepository;
import com.testplatform.repository.TestEnvironmentRepository;
import com.testplatform.testing.TestExecutor;
import com.testplatform.testing.TestExecutorFactory;
import com.testplatform.testing.TestExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
public class TestExecutionServiceImpl implements TestExecutionService {
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private TestEnvironmentRepository testEnvironmentRepository;
    
    @Autowired
    private TestExecutorFactory testExecutorFactory;
    
    @Autowired
    private TestReportGenerationService testReportGenerationService;
    
    @Override
    public List<TestExecution> getAllExecutions() {
        return testExecutionRepository.findAll();
    }
    
    @Override
    public Optional<TestExecution> getExecutionById(String id) {
        return testExecutionRepository.findById(id);
    }
    
    @Override
    public List<TestExecution> getExecutionsBySuiteId(String suiteId) {
        return testExecutionRepository.findBySuiteId(suiteId);
    }
    
    @Override
    public TestExecution createExecution(TestExecution execution) {
        if (execution.getStartTime() == null) {
            execution.setStartTime(LocalDateTime.now());
        }
        return testExecutionRepository.save(execution);
    }
    
    @Override
    public TestExecution updateExecution(String id, TestExecution execution) {
        execution.setId(id);
        if (execution.getStartTime() == null) {
            execution.setStartTime(LocalDateTime.now());
        }
        return testExecutionRepository.save(execution);
    }
    
    @Override
    public void deleteExecution(String id) {
        testExecutionRepository.deleteById(id);
    }
    
    @Override
    public List<TestExecution> getExecutionsByStatus(TestExecution.ExecutionStatus status) {
        return testExecutionRepository.findByStatus(status);
    }
    
    @Override
    public TestExecution executeTest(String suiteId) {
        // 创建新的测试执行记录
        TestExecution execution = new TestExecution();
        execution.setId(UUID.randomUUID().toString());
        execution.setSuiteId(suiteId);
        execution.setStatus(TestExecution.ExecutionStatus.RUNNING);
        execution.setStartTime(LocalDateTime.now());
        
        // 保存执行记录
        TestExecution savedExecution = testExecutionRepository.save(execution);
        
        // 异步执行测试
        new Thread(() -> {
            try {
                // 获取测试套件中的所有测试用例
                List<TestCase> testCases = testCaseRepository.findBySuiteId(suiteId);
                
                // 获取启用的测试环境
                List<TestEnvironment> activeEnvironments = testEnvironmentRepository.findByIsActiveTrueOrderByCreatedAtDesc();
                TestEnvironment testEnvironment = activeEnvironments.isEmpty() ? null : activeEnvironments.get(0);
                
                // 执行每个测试用例
                int passed = 0;
                int failed = 0;
                List<TestExecutionResult> results = new ArrayList<>();
                
                for (TestCase testCase : testCases) {
                    try {
                        // 获取相应的测试执行器
                        TestExecutor executor = testExecutorFactory.getExecutor(testCase);
                        
                        // 执行测试
                        TestExecutionResult result = executor.execute(testCase, testEnvironment);
                        // 设置测试用例信息
                        result.setTestCaseId(testCase.getId());
                        result.setTestCaseName(testCase.getName());
                        result.setTestType(testCase.getType().name());
                        
                        results.add(result);
                        
                        if (result.isSuccess()) {
                            passed++;
                        } else {
                            failed++;
                        }
                    } catch (Exception e) {
                        // 根据错误信息创建 TestExecutionResult 实例，需确保使用正确的构造函数
                        TestExecutionResult errorResult = new TestExecutionResult();
                        errorResult.setSuccess(false);
                        errorResult.setMessage("执行异常: " + e.getMessage());
                        errorResult.setTestCaseId(testCase.getId());
                        errorResult.setTestCaseName(testCase.getName());
                        errorResult.setTestType(testCase.getType().name());
                        errorResult.setErrorDetails(e.toString());
                        results.add(errorResult);
                        failed++;
                    }
                }
                
                // 更新执行记录
                savedExecution.setStatus(TestExecution.ExecutionStatus.COMPLETED);
                savedExecution.setEndTime(LocalDateTime.now());
                
                // 使用Jackson将结果列表转换为JSON
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                String jsonResults = mapper.writeValueAsString(results);
                
                savedExecution.setResult(String.format(
                    "{\"passed\": %d, \"failed\": %d, \"total\": %d, \"results\": %s}", 
                    passed, failed, passed + failed, jsonResults));
                
                testExecutionRepository.save(savedExecution);
                
                // 生成测试报告
                try {
                    testReportGenerationService.generateReport(savedExecution.getId());
                } catch (Exception e) {
                    // 记录报告生成错误，但不影响测试执行结果
                    e.printStackTrace();
                }
            } catch (Exception e) {
                // 更新执行记录为失败状态
                savedExecution.setStatus(TestExecution.ExecutionStatus.FAILED);
                savedExecution.setEndTime(LocalDateTime.now());
                savedExecution.setResult("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
                
                testExecutionRepository.save(savedExecution);
            }
        }).start();
        
        return savedExecution;
    }
    
    @Override
    public TestExecution stopExecution(String id) {
        Optional<TestExecution> existingExecution = testExecutionRepository.findById(id);
        if (existingExecution.isPresent()) {
            TestExecution execution = existingExecution.get();
            execution.setStatus(TestExecution.ExecutionStatus.FAILED);
            execution.setEndTime(LocalDateTime.now());
            return testExecutionRepository.save(execution);
        }
        return null;
    }
}