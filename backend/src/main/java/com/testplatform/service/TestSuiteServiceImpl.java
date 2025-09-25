package com.testplatform.service;

import com.testplatform.model.TestSuite;
import com.testplatform.model.TestSuiteCase;
import com.testplatform.model.TestCase;
import com.testplatform.repository.TestSuiteRepository;
import com.testplatform.repository.TestSuiteCaseRepository;
import com.testplatform.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteServiceImpl.class);
    
    @Autowired
    private TestSuiteRepository testSuiteRepository;
    
    @Autowired
    private TestSuiteCaseRepository testSuiteCaseRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
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
        
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Saving test suite with ID: {}", testSuite.getId());
        TestSuite savedTestSuite = testSuiteRepository.save(testSuite);
        logger.info("Saved test suite, returned ID: {}", savedTestSuite.getId());
        
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
        
        // 保留原有的created_at
        testSuite.setId(id);
        testSuite.setCreatedAt(existingTestSuite.getCreatedAt());
        testSuite.setUpdatedAt(LocalDateTime.now());
        
        // 先删除现有的关联关系
        testSuiteCaseRepository.deleteBySuiteId(id);
        
        // 保存测试套件
        TestSuite savedTestSuite = testSuiteRepository.save(testSuite);
        
        // 重新创建关联关系（如果有的话）
        if (testSuite.getTestSuiteCases() != null && !testSuite.getTestSuiteCases().isEmpty()) {
            for (TestSuiteCase testSuiteCase : testSuite.getTestSuiteCases()) {
                // 确保设置正确的suiteId
                testSuiteCase.setId(java.util.UUID.randomUUID().toString());
                testSuiteCase.setSuiteId(id);
                testSuiteCase.setCreatedAt(LocalDateTime.now());
                testSuiteCaseRepository.save(testSuiteCase);
            }
        }
        
        return savedTestSuite;
    }
    
    @Override
    public void deleteTestSuite(String id) {
        testSuiteRepository.deleteById(id);
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
}