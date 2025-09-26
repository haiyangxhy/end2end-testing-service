package com.testplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestCase;
import com.testplatform.model.TestCaseExecution;
import com.testplatform.repository.TestCaseRepository;
import com.testplatform.repository.TestCaseExecutionRepository;
import com.testplatform.repository.TestSuiteCaseRepository;
import com.testplatform.repository.TaskExecutionHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Primary
public class TestCaseServiceImpl implements TestCaseService {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseServiceImpl.class);
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Autowired
    private TestCaseExecutionRepository testCaseExecutionRepository;
    
    @Autowired
    private TestSuiteCaseRepository testSuiteCaseRepository;
    
    @Autowired
    private TaskExecutionHistoryRepository taskExecutionHistoryRepository;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }
    
    @Override
    public Optional<TestCase> getTestCaseById(String id) {
        return testCaseRepository.findById(id);
    }
    
    // 移除getTestCasesBySuiteId方法，因为现在通过TestSuiteCase关联
    
    @Override
    public TestCase createTestCase(TestCase testCase) {
        logger.info("Creating test case, initial ID: {}", testCase.getId());
        
        // 确保在保存前生成ID
        if (testCase.getId() == null || testCase.getId().isEmpty()) {
            logger.info("ID is null or empty, generating new ID");
            testCase.generateId();
            logger.info("ID after generateId(): {}", testCase.getId());
        } else {
            logger.info("ID already exists: {}", testCase.getId());
        }
        
        // 如果ID仍然为空，则手动创建一个
        if (testCase.getId() == null || testCase.getId().isEmpty()) {
            String newId = UUID.randomUUID().toString();
            testCase.setId(newId);
            logger.info("Manually generated ID: {}", newId);
        }
        
        // 设置创建者
        String currentUserId = getCurrentUserId();
        testCase.setCreatedBy(currentUserId);
        logger.info("Set createdBy to: {}", currentUserId);
        
        testCase.setCreatedAt(LocalDateTime.now());
        testCase.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Saving test case with ID: {}", testCase.getId());
        TestCase savedTestCase = testCaseRepository.save(testCase);
        logger.info("Saved test case, returned ID: {}", savedTestCase.getId());
        
        return savedTestCase;
    }
    
    @Override
    public TestCase updateTestCase(String id, TestCase testCase) {
        // 先获取现有的测试用例以保留created_at和created_by
        Optional<TestCase> existingTestCaseOpt = testCaseRepository.findById(id);
        if (existingTestCaseOpt.isPresent()) {
            TestCase existingTestCase = existingTestCaseOpt.get();
            
            // 保留原有的created_at和created_by
            testCase.setId(id);
            testCase.setCreatedAt(existingTestCase.getCreatedAt());
            testCase.setCreatedBy(existingTestCase.getCreatedBy());
            testCase.setUpdatedAt(LocalDateTime.now());
            
            return testCaseRepository.save(testCase);
        } else {
            throw new RuntimeException("测试用例不存在: " + id);
        }
    }
    
    @Override
    @Transactional
    public void deleteTestCase(String id) {
        logger.info("开始删除测试用例: {}", id);
        
        // 1. 检查是否存在测试用例执行记录
        List<TestCaseExecution> executions = testCaseExecutionRepository.findByTestCaseId(id);
        if (!executions.isEmpty()) {
            // 检查是否存在任务执行历史记录
            List<String> executionIds = executions.stream()
                    .map(TestCaseExecution::getExecutionId)
                    .collect(java.util.stream.Collectors.toList());
            
            if (taskExecutionHistoryRepository.existsByExecutionIdIn(executionIds)) {
                throw new RuntimeException("无法删除测试用例，存在关联的任务执行历史记录。请先删除相关的执行记录。");
            }
            
            // 如果没有历史记录，可以删除执行记录
            logger.info("删除测试用例执行记录: {}", id);
            testCaseExecutionRepository.deleteByTestCaseId(id);
        }
        
        // 2. 删除测试套件关联记录
        logger.info("删除测试套件关联记录: {}", id);
        testSuiteCaseRepository.deleteByTestCaseId(id);
        
        // 3. 最后删除测试用例本身
        logger.info("删除测试用例: {}", id);
        testCaseRepository.deleteById(id);
        
        logger.info("测试用例删除完成: {}", id);
    }
    
    // 移除按类型查询方法实现，测试用例不再有类型字段
    
    @Override
    public List<TestCase> getTestCasesByPriority(TestCase.Priority priority) {
        return testCaseRepository.findByPriority(priority);
    }
    
    @Override
    public List<TestCase> getTestCasesByStatus(TestCase.Status status) {
        return testCaseRepository.findByStatus(status);
    }
    
    @Override
    public List<TestCase> getActiveTestCases() {
        return testCaseRepository.findByIsActive(true);
    }
    
    // 移除按类型和激活状态查询方法实现，测试用例不再有类型字段
    
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