package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.model.TestSuite;
import com.testplatform.model.TestSuiteCase;
import com.testplatform.model.TestCase;
import com.testplatform.repository.TestSuiteRepository;
import com.testplatform.repository.TestSuiteCaseRepository;
import com.testplatform.repository.TestCaseRepository;
import com.testplatform.repository.TestExecutionRepository;
import com.testplatform.repository.TaskExecutionHistoryRepository;
import com.testplatform.repository.ScheduledTaskRepository;
import com.testplatform.model.ScheduledTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteServiceImpl.class);
    
    @Autowired
    private TestSuiteRepository testSuiteRepository;
    
    @Autowired
    private TestSuiteCaseRepository testSuiteCaseRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
    @Autowired
    private TaskExecutionHistoryRepository taskExecutionHistoryRepository;

    @Autowired
    private ScheduledTaskRepository scheduledTaskRepository;
    
    @Override
    public List<TestSuite> getAllTestSuites() {
        return testSuiteRepository.findAll();
    }
    
    @Override
    public Optional<TestSuite> getTestSuiteById(String id) {
        return testSuiteRepository.findById(id);
    }
    
    @Override
    public TestSuite createTestSuite(TestSuite testSuite) {
        logger.info("Creating test suite, initial ID: {}", testSuite.getId());
        
        // 确保在保存前生成ID
        if (testSuite.getId() == null || testSuite.getId().isEmpty()) {
            logger.info("ID is null or empty, generating new ID");
            testSuite.generateId();
            logger.info("ID after generateId(): {}", testSuite.getId());
        } else {
            logger.info("ID already exists: {}", testSuite.getId());
        }
        
        // 如果ID仍然为空，则手动创建一个
        if (testSuite.getId() == null || testSuite.getId().isEmpty()) {
            String newId = UUID.randomUUID().toString();
            testSuite.setId(newId);
            logger.info("Manually generated ID: {}", newId);
        }
        
        // 设置创建者
        String currentUserId = getCurrentUserId();
        testSuite.setCreatedBy(currentUserId);
        logger.info("Set createdBy to: {}", currentUserId);
        
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setUpdatedAt(LocalDateTime.now());
        
        // 处理测试用例关联关系
        if (testSuite.getTestSuiteCases() != null && !testSuite.getTestSuiteCases().isEmpty()) {
            for (TestSuiteCase testSuiteCase : testSuite.getTestSuiteCases()) {
                // 确保设置正确的suiteId
                testSuiteCase.setId(java.util.UUID.randomUUID().toString());
                testSuiteCase.setSuiteId(testSuite.getId());
                testSuiteCase.setCreatedAt(LocalDateTime.now());
            }
        }
        
        logger.info("Saving test suite with ID: {}", testSuite.getId());
        TestSuite savedTestSuite = testSuiteRepository.save(testSuite);
        logger.info("Saved test suite, returned ID: {}", savedTestSuite.getId());
        
        // 保存测试用例关联关系
        if (savedTestSuite.getTestSuiteCases() != null && !savedTestSuite.getTestSuiteCases().isEmpty()) {
            testSuiteCaseRepository.saveAll(savedTestSuite.getTestSuiteCases());
        }
        
        return savedTestSuite;
    }
    
    @Override
    public TestSuite updateTestSuite(String id, TestSuite testSuite) {
        // 先获取现有的测试套件以保留created_at
        Optional<TestSuite> existingTestSuiteOpt = testSuiteRepository.findById(id);
        if (!existingTestSuiteOpt.isPresent()) {
            throw new RuntimeException("测试套件不存在: " + id);
        }
        
        TestSuite existingTestSuite = existingTestSuiteOpt.get();
        
        // 保留原有的created_at和created_by
        testSuite.setId(id);
        testSuite.setCreatedAt(existingTestSuite.getCreatedAt());
        testSuite.setCreatedBy(existingTestSuite.getCreatedBy());
        testSuite.setUpdatedAt(LocalDateTime.now());
        
        // 先删除现有的关联关系
        testSuiteCaseRepository.deleteBySuiteId(id);

        // 重新创建关联关系（如果有的话）
        if (testSuite.getTestSuiteCases() != null && !testSuite.getTestSuiteCases().isEmpty()) {
            for (TestSuiteCase testSuiteCase : testSuite.getTestSuiteCases()) {
                // 确保设置正确的suiteId
                testSuiteCase.setId(java.util.UUID.randomUUID().toString());
                testSuiteCase.setSuiteId(id);
                testSuiteCase.setCreatedAt(LocalDateTime.now());
            }
        }
        
        // 保存测试套件
        TestSuite savedTestSuite = testSuiteRepository.save(testSuite);

        testSuiteCaseRepository.saveAll(testSuite.getTestSuiteCases());
        
        return savedTestSuite;
    }
    
    @Override
    @Transactional
    public void deleteTestSuite(String id) {
        logger.info("开始删除测试套件: {}", id);
        
        // 1. 检查是否存在测试执行记录
        List<TestExecution> executions = testExecutionRepository.findBySuiteId(id);
        if (!executions.isEmpty()) {
            // 检查是否存在任务执行历史记录
            List<String> executionIds = executions.stream()
                    .map(TestExecution::getId)
                    .collect(java.util.stream.Collectors.toList());
            
            if (taskExecutionHistoryRepository.existsByExecutionIdIn(executionIds)) {
                throw new RuntimeException("无法删除测试套件，存在关联的任务执行历史记录。请先删除相关的执行记录。");
            }
            
            // 如果没有历史记录，可以删除执行记录
            logger.info("删除测试执行记录: {}", id);
            testExecutionRepository.deleteBySuiteId(id);
        }
        
        // 2. 检查是否存在定时任务记录
        List<ScheduledTask> scheduledTasks = scheduledTaskRepository.findBySuiteId(id);
        if (!scheduledTasks.isEmpty()) {
            throw new RuntimeException("无法删除测试套件，存在关联的定时任务记录。请先删除相关的定时任务。");
        }

        // 3. 删除测试套件关联记录
        logger.info("删除测试套件关联记录: {}", id);
        testSuiteCaseRepository.deleteBySuiteId(id);

        // 4. 最后删除测试套件本身
        logger.info("删除测试套件: {}", id);
        testSuiteRepository.deleteById(id);
        
        logger.info("测试套件删除完成: {}", id);
    }
    
    @Override
    public List<TestSuite> getTestSuitesByType(TestSuite.TestSuiteType type) {
        return testSuiteRepository.findByType(type);
    }
    
    /**
     * 获取测试套件中按执行顺序排列的测试用例
     * 排序规则：1. 优先级 2. 执行顺序 3. 创建时间
     */
    public List<TestCase> getOrderedTestCases(String suiteId) {
        // 获取测试套件关联的测试用例
        List<TestSuiteCase> suiteCases = testSuiteCaseRepository.findBySuiteIdOrderByExecutionOrder(suiteId);
        
        // 获取测试用例详情
        List<TestCase> testCases = new ArrayList<>();
        for (TestSuiteCase suiteCase : suiteCases) {
            if (suiteCase.getIsEnabled()) { // 只包含启用的测试用例
                TestCase testCase = testCaseRepository.findById(suiteCase.getTestCaseId()).orElse(null);
                if (testCase != null) {
                    testCases.add(testCase);
                }
            }
        }
        
        // 按优先级和执行顺序排序
        testCases.sort((tc1, tc2) -> {
            // 1. 首先按优先级排序
            int priorityComparison = getPriorityOrder(tc1.getPriority().name()) - getPriorityOrder(tc2.getPriority().name());
            if (priorityComparison != 0) {
                return priorityComparison;
            }
            
            // 2. 然后按执行顺序排序
            TestSuiteCase suiteCase1 = suiteCases.stream()
                .filter(sc -> sc.getTestCaseId().equals(tc1.getId()))
                .findFirst().orElse(null);
            TestSuiteCase suiteCase2 = suiteCases.stream()
                .filter(sc -> sc.getTestCaseId().equals(tc2.getId()))
                .findFirst().orElse(null);
                
            if (suiteCase1 != null && suiteCase2 != null) {
                int orderComparison = suiteCase1.getExecutionOrder().compareTo(suiteCase2.getExecutionOrder());
                if (orderComparison != 0) {
                    return orderComparison;
                }
            }
            
            // 3. 最后按创建时间排序
            return tc1.getCreatedAt().compareTo(tc2.getCreatedAt());
        });
        
        return testCases;
    }
    
    /**
     * 获取优先级排序值
     */
    private int getPriorityOrder(String priority) {
        switch (priority) {
            case "CRITICAL": return 1;
            case "HIGH": return 2;
            case "MEDIUM": return 3;
            case "LOW": return 4;
            default: return 5;
        }
    }
    
    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // 从认证信息中获取用户ID
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
                    // 根据用户名映射到用户ID
                    return mapUsernameToUserId(username);
                } else if (principal instanceof String) {
                    return mapUsernameToUserId((String) principal);
                }
            }
        } catch (Exception e) {
            logger.warn("无法获取当前用户ID，使用默认值", e);
        }
        
        // 如果无法获取当前用户，返回默认的管理员用户ID
        return "admin-001";
    }
    
    /**
     * 将用户名映射到用户ID
     */
    private String mapUsernameToUserId(String username) {
        // 简单的映射逻辑，实际应用中可以从数据库查询
        if ("admin".equals(username)) {
            return "admin-001";
        }
        // 其他用户暂时返回默认值
        return "admin-001";
    }
}