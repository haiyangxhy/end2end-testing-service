package com.testplatform.service;

import com.testplatform.model.TestExecution;
import com.testplatform.repository.TestExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestExecutionServiceImpl implements TestExecutionService {
    
    @Autowired
    private TestExecutionRepository testExecutionRepository;
    
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
}