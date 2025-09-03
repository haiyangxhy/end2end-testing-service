package com.testplatform.controller;

import com.testplatform.model.TestSuite;
import com.testplatform.service.TestSuiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-suites")
@CrossOrigin(origins = "*")
public class TestSuiteController {
    
    @Autowired
    private TestSuiteService testSuiteService;
    
    @GetMapping
    public ResponseEntity<List<TestSuite>> getAllTestSuites() {
        List<TestSuite> testSuites = testSuiteService.getAllTestSuites();
        return new ResponseEntity<>(testSuites, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestSuite> getTestSuiteById(@PathVariable String id) {
        Optional<TestSuite> testSuite = testSuiteService.getTestSuiteById(id);
        if (testSuite.isPresent()) {
            return new ResponseEntity<>(testSuite.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TestSuite> createTestSuite(@RequestBody TestSuite testSuite) {
        TestSuite createdTestSuite = testSuiteService.createTestSuite(testSuite);
        return new ResponseEntity<>(createdTestSuite, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestSuite> updateTestSuite(@PathVariable String id, @RequestBody TestSuite testSuite) {
        TestSuite updatedTestSuite = testSuiteService.updateTestSuite(id, testSuite);
        return new ResponseEntity<>(updatedTestSuite, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTestSuite(@PathVariable String id) {
        testSuiteService.deleteTestSuite(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TestSuite>> getTestSuitesByType(@PathVariable TestSuite.TestSuiteType type) {
        List<TestSuite> testSuites = testSuiteService.getTestSuitesByType(type);
        return new ResponseEntity<>(testSuites, HttpStatus.OK);
    }
}