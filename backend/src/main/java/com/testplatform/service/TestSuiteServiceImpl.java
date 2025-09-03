package com.testplatform.service;

import com.testplatform.model.TestSuite;
import com.testplatform.repository.TestSuiteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    
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
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setUpdatedAt(LocalDateTime.now());
        return testSuiteRepository.save(testSuite);
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