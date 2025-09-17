package com.testplatform.service;

import com.testplatform.model.TestSuite;
import com.testplatform.repository.TestSuiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteServiceImpl.class);
    
    @Autowired
    private TestSuiteRepository testSuiteRepository;
    
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
        testSuite.setId(id);
        testSuite.setUpdatedAt(LocalDateTime.now());
        return testSuiteRepository.save(testSuite);
    }
    
    @Override
    public void deleteTestSuite(String id) {
        testSuiteRepository.deleteById(id);
    }
    
    @Override
    public List<TestSuite> getTestSuitesByType(TestSuite.TestSuiteType type) {
        return testSuiteRepository.findByType(type);
    }
}