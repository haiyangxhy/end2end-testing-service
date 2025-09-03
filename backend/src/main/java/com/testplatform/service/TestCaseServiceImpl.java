package com.testplatform.service;

import com.testplatform.model.TestCase;
import com.testplatform.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestCaseServiceImpl implements TestCaseService {
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    @Override
    public List<TestCase> getAllTestCases() {
        return testCaseRepository.findAll();
    }
    
    @Override
    public Optional<TestCase> getTestCaseById(String id) {
        return testCaseRepository.findById(id);
    }
    
    @Override
    public List<TestCase> getTestCasesBySuiteId(String suiteId) {
        return testCaseRepository.findBySuiteId(suiteId);
    }
    
    @Override
    public TestCase createTestCase(TestCase testCase) {
        testCase.setCreatedAt(LocalDateTime.now());
        testCase.setUpdatedAt(LocalDateTime.now());
        return testCaseRepository.save(testCase);
    }
    
    @Override
    public TestCase updateTestCase(String id, TestCase testCase) {
        testCase.setId(id);
        testCase.setUpdatedAt(LocalDateTime.now());
        return testCaseRepository.save(testCase);
    }
    
    @Override
    public void deleteTestCase(String id) {
        testCaseRepository.deleteById(id);
    }
    
    @Override
    public List<TestCase> getTestCasesByType(TestCase.TestCaseType type) {
        return testCaseRepository.findByType(type);
    }
}