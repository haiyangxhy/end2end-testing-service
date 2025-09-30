package com.testplatform.controller;

import com.testplatform.model.TestCase;
import com.testplatform.service.TestCaseService;
import com.testplatform.service.TestCaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test-cases")
@CrossOrigin(origins = "*")
public class TestCaseController {
    private static final Logger logger = LoggerFactory.getLogger(TestCaseController.class);
    
    @Autowired
    private TestCaseService testCaseService;
    
    @GetMapping
    public ResponseEntity<List<TestCase>> getAllTestCases() {
        logger.info("Getting all test cases");
        List<TestCase> testCases = testCaseService.getAllTestCases();
        logger.info("Found {} test cases", testCases.size());
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TestCase> getTestCaseById(@PathVariable String id) {
        logger.info("Getting test case by ID: {}", id);
        Optional<TestCase> testCase = testCaseService.getTestCaseById(id);
        if (testCase.isPresent()) {
            logger.info("Found test case with ID: {}", id);
            return new ResponseEntity<>(testCase.get(), HttpStatus.OK);
        } else {
            logger.info("Test case not found with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    // 移除getTestCasesBySuiteId方法，因为现在通过TestSuiteCase关联
    
    @PostMapping
    public ResponseEntity<TestCase> createTestCase(@RequestBody TestCase testCase) {
        logger.info("Creating test case, received ID: {}", testCase.getId());
        TestCase createdTestCase = testCaseService.createTestCase(testCase);
        logger.info("Created test case, returned ID: {}", createdTestCase.getId());
        return new ResponseEntity<>(createdTestCase, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<TestCase> updateTestCase(@PathVariable String id, @RequestBody TestCase testCase) {
        TestCase updatedTestCase = testCaseService.updateTestCase(id, testCase);
        if (updatedTestCase != null) {
            return new ResponseEntity<>(updatedTestCase, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTestCase(@PathVariable String id) {
        try {
            testCaseService.deleteTestCase(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            logger.error("删除测试用例失败: {}", e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error("删除测试用例时发生未知错误: {}", e.getMessage(), e);
            return new ResponseEntity<>("删除测试用例失败: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // 移除按类型查询的API，测试用例不再有类型字段
    
    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<TestCase>> getTestCasesByPriority(@PathVariable TestCase.Priority priority) {
        List<TestCase> testCases = testCaseService.getTestCasesByPriority(priority);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<TestCase>> getTestCasesByStatus(@PathVariable TestCase.Status status) {
        List<TestCase> testCases = testCaseService.getTestCasesByStatus(status);
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<TestCase>> getActiveTestCases() {
        List<TestCase> testCases = testCaseService.getActiveTestCases();
        return new ResponseEntity<>(testCases, HttpStatus.OK);
    }
    
    // 移除按类型和激活状态查询的API，测试用例不再有类型字段
}