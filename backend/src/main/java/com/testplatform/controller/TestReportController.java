package com.testplatform.controller;

import com.testplatform.model.TestReport;
import com.testplatform.service.TestReportGenerationService;
import com.testplatform.service.TestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-reports")
@CrossOrigin(origins = "http://localhost:5173")
public class TestReportController {
    
    @Autowired
    private TestReportGenerationService testReportGenerationService;
    
    @Autowired
    private TestReportService testReportService;
    
    @PostMapping("/generate/{executionId}")
    public ResponseEntity<TestReport> generateReport(@PathVariable String executionId) {
        try {
            TestReport report = testReportGenerationService.generateReport(executionId);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<TestReport>> getAllReports() {
        List<TestReport> reports = testReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestReport> getReportById(@PathVariable String id) {
        Optional<TestReport> report = testReportService.getReportById(id);
        return report.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<TestReport>> getReportsBySuiteId(@PathVariable String suiteId) {
        List<TestReport> reports = testReportService.getReportsBySuiteId(suiteId);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<TestReport>> getReportsByExecutionId(@PathVariable String executionId) {
        List<TestReport> reports = testReportService.getReportsByExecutionId(executionId);
        return ResponseEntity.ok(reports);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable String id) {
        testReportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}