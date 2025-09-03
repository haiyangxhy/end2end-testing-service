package com.testplatform.controller;

import com.testplatform.model.TestCase;
import com.testplatform.service.TestCaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-cases")
@CrossOrigin(origins = "*")
public class TestCaseController {
    
    @Autowired
    private TestCaseService testCaseService;
    
    @GetMapping
    public ResponseEntity<List<TestCase>> getAllTestCases() {
        List<TestCase> testCases = testCaseService.getAllTestCases();
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCaseById(@PathVariable String id) {
        Optional<TestCase> testCase = testCaseService.getTestCaseById(id);
        if (testCase.isPresent()) {
            return new ResponseEntity<>(testCase.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<TestCase>> getTestCasesBySuiteId(@PathVariable String suiteId) {
        List<TestCase> testCases = testCaseService.getTestCasesBySuiteId(suiteId);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @PostMapping
    public ResponseEntity<TestCase> createTestCase(@RequestBody TestCase testCase) {
        TestCase createdTestCase = testCaseService.createTestCase(testCase);
        return new ResponseEntity<>(createdTestCase, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestCase> updateTestCase(@PathVariable String id, @RequestBody TestCase testCase) {
        TestCase updatedTestCase = testCaseService.updateTestCase(id, testCase);
        return new ResponseEntity<>(updatedTestCase, HttpStatus.OK);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteTestCase(@PathVariable String id) {
        testCaseService.deleteTestCase(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<TestCase>> getTestCasesByType(@PathVariable TestCase.TestCaseType type) {
        List<TestCase> testCases = testCaseService.getTestCasesByType(type);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
}