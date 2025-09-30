package com.testplatform.controller;

import com.testplatform.model.TestSuiteCase;
import com.testplatform.model.TestCase;
import com.testplatform.repository.TestSuiteCaseRepository;
import com.testplatform.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/test-suite-cases")
@CrossOrigin(origins = "*")
public class TestSuiteCaseController {
    private static final Logger logger = LoggerFactory.getLogger(TestSuiteCaseController.class);
    
    @Autowired
    private TestSuiteCaseRepository testSuiteCaseRepository;
    
    @Autowired
    private TestCaseRepository testCaseRepository;
    
    /**
     * 获取测试套件关联的所有测试用例（包含测试用例详情）
     */
    @GetMapping("/suite/{suiteId}")
    public ResponseEntity<List<TestSuiteCase>> getTestSuiteCases(@PathVariable String suiteId) {
        logger.info("Getting test suite cases for suite ID: {}", suiteId);
        try {
            List<TestSuiteCase> suiteCases = testSuiteCaseRepository.findBySuiteIdOrderByExecutionOrder(suiteId);
            
            // 为每个TestSuiteCase填充测试用例详情
            for (TestSuiteCase suiteCase : suiteCases) {
                TestCase testCase = testCaseRepository.findById(suiteCase.getTestCaseId()).orElse(null);
                if (testCase != null) {
                    // 这里需要创建一个包含测试用例详情的DTO
                    // 由于TestSuiteCase模型中没有testCase字段，我们需要通过其他方式处理
                    // 暂时先返回基本的TestSuiteCase信息
                }
            }
            
            logger.info("Found {} test suite cases for suite ID: {}", suiteCases.size(), suiteId);
            return new ResponseEntity<>(suiteCases, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting test suite cases for suite ID: {}", suiteId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 获取测试套件关联的测试用例详情（用于前端显示）
     */
    @GetMapping("/suite/{suiteId}/with-details")
    public ResponseEntity<List<TestSuiteCaseWithDetails>> getTestSuiteCasesWithDetails(@PathVariable String suiteId) {
        logger.info("Getting test suite cases with details for suite ID: {}", suiteId);
        try {
            List<TestSuiteCase> suiteCases = testSuiteCaseRepository.findBySuiteIdOrderByExecutionOrder(suiteId);
            
            List<TestSuiteCaseWithDetails> result = suiteCases.stream()
                .map(suiteCase -> {
                    TestCase testCase = testCaseRepository.findById(suiteCase.getTestCaseId()).orElse(null);
                    return new TestSuiteCaseWithDetails(suiteCase, testCase);
                })
                .collect(Collectors.toList());
            
            logger.info("Found {} test suite cases with details for suite ID: {}", result.size(), suiteId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error getting test suite cases with details for suite ID: {}", suiteId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 添加测试用例到测试套件
     */
    @PostMapping
    public ResponseEntity<TestSuiteCase> addTestCaseToSuite(@RequestBody AddTestCaseToSuiteRequest request) {
        logger.info("Adding test case {} to suite {}", request.getTestCaseId(), request.getSuiteId());
        try {
            // 检查是否已经存在关联
            TestSuiteCase existing = testSuiteCaseRepository.findBySuiteIdAndTestCaseId(
                request.getSuiteId(), request.getTestCaseId());
            if (existing != null) {
                logger.warn("Test case {} already exists in suite {}", request.getTestCaseId(), request.getSuiteId());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            
            // 获取下一个执行顺序
            List<TestSuiteCase> existingCases = testSuiteCaseRepository.findBySuiteIdOrderByExecutionOrder(request.getSuiteId());
            int nextOrder = existingCases.isEmpty() ? 1 : existingCases.size() + 1;
            
            TestSuiteCase newSuiteCase = new TestSuiteCase();
            newSuiteCase.setId(java.util.UUID.randomUUID().toString());
            newSuiteCase.setSuiteId(request.getSuiteId());
            newSuiteCase.setTestCaseId(request.getTestCaseId());
            newSuiteCase.setExecutionOrder(nextOrder);
            newSuiteCase.setIsEnabled(true);
            newSuiteCase.setUpdatedAt(LocalDateTime.now());
            
            TestSuiteCase saved = testSuiteCaseRepository.save(newSuiteCase);
            logger.info("Successfully added test case {} to suite {}", request.getTestCaseId(), request.getSuiteId());
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error adding test case to suite", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新测试套件用例的执行顺序
     */
    @PutMapping("/{id}/order")
    public ResponseEntity<TestSuiteCase> updateExecutionOrder(@PathVariable String id, @RequestBody UpdateOrderRequest request) {
        logger.info("Updating execution order for test suite case {}", id);
        try {
            TestSuiteCase suiteCase = testSuiteCaseRepository.findById(id).orElse(null);
            if (suiteCase == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            suiteCase.setExecutionOrder(request.getExecutionOrder());
            suiteCase.setUpdatedAt(LocalDateTime.now());
            TestSuiteCase updated = testSuiteCaseRepository.save(suiteCase);
            logger.info("Successfully updated execution order for test suite case {}", id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error updating execution order for test suite case {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 切换测试套件用例的启用状态
     */
    @PutMapping("/{id}/toggle")
    public ResponseEntity<TestSuiteCase> toggleEnabled(@PathVariable String id) {
        logger.info("Toggling enabled status for test suite case {}", id);
        try {
            TestSuiteCase suiteCase = testSuiteCaseRepository.findById(id).orElse(null);
            if (suiteCase == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            suiteCase.setIsEnabled(!suiteCase.getIsEnabled());
            suiteCase.setUpdatedAt(LocalDateTime.now());
            TestSuiteCase updated = testSuiteCaseRepository.save(suiteCase);
            logger.info("Successfully toggled enabled status for test suite case {}", id);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error toggling enabled status for test suite case {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 从测试套件中删除测试用例
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeTestCaseFromSuite(@PathVariable String id) {
        logger.info("Removing test suite case {}", id);
        try {
            testSuiteCaseRepository.deleteById(id);
            logger.info("Successfully removed test suite case {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            logger.error("Error removing test suite case {}", id, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 测试套件用例详情DTO
     */
    public static class TestSuiteCaseWithDetails {
        private String id;
        private String suiteId;
        private String testCaseId;
        private TestCase testCase;
        private Integer executionOrder;
        private Boolean isEnabled;
        private String createdAt;
        private String updatedAt;
        
        public TestSuiteCaseWithDetails(TestSuiteCase suiteCase, TestCase testCase) {
            this.id = suiteCase.getId();
            this.suiteId = suiteCase.getSuiteId();
            this.testCaseId = suiteCase.getTestCaseId();
            this.testCase = testCase;
            this.executionOrder = suiteCase.getExecutionOrder();
            this.isEnabled = suiteCase.getIsEnabled();
            this.createdAt = suiteCase.getCreatedAt().toString();
            this.updatedAt = suiteCase.getUpdatedAt() != null ? suiteCase.getUpdatedAt().toString() : null;
        }
        
        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getSuiteId() { return suiteId; }
        public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
        
        public String getTestCaseId() { return testCaseId; }
        public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
        
        public TestCase getTestCase() { return testCase; }
        public void setTestCase(TestCase testCase) { this.testCase = testCase; }
        
        public Integer getExecutionOrder() { return executionOrder; }
        public void setExecutionOrder(Integer executionOrder) { this.executionOrder = executionOrder; }
        
        public Boolean getIsEnabled() { return isEnabled; }
        public void setIsEnabled(Boolean isEnabled) { this.isEnabled = isEnabled; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    }
    
    /**
     * 添加测试用例到套件的请求DTO
     */
    public static class AddTestCaseToSuiteRequest {
        private String suiteId;
        private String testCaseId;
        
        public String getSuiteId() { return suiteId; }
        public void setSuiteId(String suiteId) { this.suiteId = suiteId; }
        
        public String getTestCaseId() { return testCaseId; }
        public void setTestCaseId(String testCaseId) { this.testCaseId = testCaseId; }
    }
    
    /**
     * 更新执行顺序的请求DTO
     */
    public static class UpdateOrderRequest {
        private Integer executionOrder;
        
        public Integer getExecutionOrder() { return executionOrder; }
        public void setExecutionOrder(Integer executionOrder) { this.executionOrder = executionOrder; }
    }
}
