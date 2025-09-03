package com.testplatform.controller;

import com.testplatform.model.TestReport;
import com.testplatform.service.TestReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class TestReportController {
    
    @Autowired
    private TestReportService testReportService;
    
    @GetMapping
    public ResponseEntity<List<TestReport>> getAllReports() {
        List<TestReport> reports = testReportService.getAllReports();
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestReport> getReportById(@PathVariable String id) {
        Optional<TestReport> report = testReportService.getReportById(id);
        if (report.isPresent()) {
            return new ResponseEntity<>(report.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<TestReport>> getReportsBySuiteId(@PathVariable String suiteId) {
        List<TestReport> reports = testReportService.getReportsBySuiteId(suiteId);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @GetMapping("/execution/{executionId}")
    public ResponseEntity<List<TestReport>> getReportsByExecutionId(@PathVariable String executionId) {
        List<TestReport> reports = testReportService.getReportsByExecutionId(executionId);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<TestReport> createReport(@RequestBody TestReport report) {
        TestReport createdReport = testReportService.createReport(report);
        return new ResponseEntity<>(createdReport, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestReport> updateReport(@PathVariable String id, @RequestBody TestReport report) {
        TestReport updatedReport = testReportService.updateReport(id, report);
        return new ResponseEntity<>(updatedReport, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteReport(@PathVariable String id) {
        testReportService.deleteReport(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/{id}/download")
    public ResponseEntity<String> downloadReport(@PathVariable String id) {
        // In a real implementation, this would generate and return the actual report file
        return new ResponseEntity<>("Report download endpoint - implementation pending", HttpStatus.OK);
    }
}