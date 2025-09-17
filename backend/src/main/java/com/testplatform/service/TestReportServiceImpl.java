package com.testplatform.service;

import com.testplatform.model.TestReport;
import com.testplatform.repository.TestReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Primary
public class TestReportServiceImpl implements TestReportService {
    
    @Autowired
    private TestReportRepository testReportRepository;
    
    @Override
    public List<TestReport> getAllReports() {
        return testReportRepository.findAll();
    }
    
    @Override
    public Optional<TestReport> getReportById(String id) {
        return testReportRepository.findById(id);
    }
    
    @Override
    public List<TestReport> getReportsBySuiteId(String suiteId) {
        return testReportRepository.findBySuiteId(suiteId);
    }
    
    @Override
    public List<TestReport> getReportsByExecutionId(String executionId) {
        return testReportRepository.findByExecutionId(executionId);
    }
    
    @Override
    public TestReport createReport(TestReport report) {
        report.setCreatedAt(LocalDateTime.now());
        return testReportRepository.save(report);
    }
    
    @Override
    public TestReport updateReport(String id, TestReport report) {
        report.setId(id);
        return testReportRepository.save(report);
    }
    
    @Override
    public void deleteReport(String id) {
        testReportRepository.deleteById(id);
    }
}