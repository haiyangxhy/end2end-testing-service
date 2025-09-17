package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestCase;
import com.testplatform.model.TargetSystemConfig;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TestCaseRepository;
import com.testplatform.repository.TargetSystemConfigRepository;
import com.testplatform.testing.TestExecutor;
import com.testplatform.testing.TestExecutorFactory;
import com.testplatform.testing.TestExecutionResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private TargetSystemConfigRepository targetSystemConfigRepository;
    
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
                
                // 获取启用的目标系统配置
                List<TargetSystemConfig> activeConfigs = targetSystemConfigRepository.findByIsActiveTrue();
                TargetSystemConfig targetSystemConfig = activeConfigs.isEmpty() ? null : activeConfigs.get(0);
                
                // 执行每个测试用例
                int passed = 0;
                int failed = 0;
                StringBuilder resultDetails = new StringBuilder();
                
                for (TestCase testCase : testCases) {
                    try {
                        // 获取相应的测试执行器
                        TestExecutor executor = testExecutorFactory.getExecutor(testCase);
                        
                        // 执行测试
                        TestExecutionResult result = executor.execute(testCase, targetSystemConfig);
                        
                        // 记录结果
                        resultDetails.append(String.format("测试用例 '%s': %s (耗时: %d ms)\n", 
                            testCase.getName(), 
                            result.isSuccess() ? "通过" : "失败", 
                            result.getExecutionTime()));
                        
                        if (result.isSuccess()) {
                            passed++;
                        } else {
                            failed++;
                        }
                    } catch (Exception e) {
                        resultDetails.append(String.format("测试用例 '%s': 执行异常 - %s\n", 
                            testCase.getName(), e.getMessage()));
                        failed++;
                    }
                }
                
                // 更新执行记录
                savedExecution.setStatus(TestExecution.ExecutionStatus.COMPLETED);
                savedExecution.setEndTime(LocalDateTime.now());
                savedExecution.setResult(String.format(
                    "{\"passed\": %d, \"failed\": %d, \"total\": %d, \"details\": \"%s\"}", 
                    passed, failed, passed + failed, resultDetails.toString().replace("\"", "\\\"")));
                
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