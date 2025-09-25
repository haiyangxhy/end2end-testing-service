package com.testplatform.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestCase;
import com.testplatform.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
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
        
        testCase.setCreatedAt(LocalDateTime.now());
        testCase.setUpdatedAt(LocalDateTime.now());
        
        logger.info("Saving test case with ID: {}", testCase.getId());
        TestCase savedTestCase = testCaseRepository.save(testCase);
        logger.info("Saved test case, returned ID: {}", savedTestCase.getId());
        
        return savedTestCase;
    }
    
    @Override
    public TestCase updateTestCase(String id, TestCase testCase) {
        // 先获取现有的测试用例以保留created_at
        Optional<TestCase> existingTestCaseOpt = testCaseRepository.findById(id);
        if (existingTestCaseOpt.isPresent()) {
            TestCase existingTestCase = existingTestCaseOpt.get();
            
            // 保留原有的created_at
            testCase.setId(id);
            testCase.setCreatedAt(existingTestCase.getCreatedAt());
            testCase.setUpdatedAt(LocalDateTime.now());
            
            return testCaseRepository.save(testCase);
        } else {
            throw new RuntimeException("测试用例不存在: " + id);
        }
    }
    
    @Override
    public void deleteTestCase(String id) {
        testCaseRepository.deleteById(id);
    }
    
    @Override
    public List<TestCase> getTestCasesByType(TestCase.TestCaseType type) {
        return testCaseRepository.findByType(type);
    }
    
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
    
    @Override
    public List<TestCase> getTestCasesByTypeAndActive(TestCase.TestCaseType type, Boolean isActive) {
        return testCaseRepository.findByTypeAndIsActive(type, isActive);
    }
}