package com.testplatform.controller;

import com.testplatform.model.TestSuite;
import com.testplatform.service.TestSuiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-suites")
@CrossOrigin(origins = "*")
public class TestSuiteController {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteController.class);
    
    @Autowired
    private TestSuiteService testSuiteService;
    
    @GetMapping
    public ResponseEntity<List<TestSuite>> getAllTestSuites() {
        logger.info("Getting all test suites");
        List<TestSuite> testSuites = testSuiteService.getAllTestSuites();
        logger.info("Found {} test suites", testSuites.size());
        return new ResponseEntity<>(testSuites, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestSuite> getTestSuiteById(@PathVariable String id) {
        logger.info("Getting test suite by ID: {}", id);
        Optional<TestSuite> testSuite = testSuiteService.getTestSuiteById(id);
        if (testSuite.isPresent()) {
            logger.info("Found test suite with ID: {}", id);
            return new ResponseEntity<>(testSuite.get(), HttpStatus.OK);
        } else {
            logger.info("Test suite not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    public ResponseEntity<TestSuite> createTestSuite(@RequestBody TestSuite testSuite) {
        logger.info("Creating test suite, received ID: {}", testSuite.getId());
        TestSuite createdTestSuite = testSuiteService.createTestSuite(testSuite);
        logger.info("Created test suite, returned ID: {}", createdTestSuite.getId());
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