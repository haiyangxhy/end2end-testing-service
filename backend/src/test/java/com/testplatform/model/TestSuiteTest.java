package com.testplatform.model;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ContextConfiguration
public class TestSuiteTest {
    
    @Test
    public void testIdGeneration() {
        TestSuite testSuite = new TestSuite();
        assertNull(testSuite.getId());
        
        // 模拟JPA的PrePersist调用
        testSuite.generateId();
        assertNotNull(testSuite.getId());
        assertFalse(testSuite.getId().isEmpty());
        
        System.out.println("Generated ID: " + testSuite.getId());
    }
    
    @Test
    public void testIdGenerationInConstructor() {
        TestSuite testSuite = new TestSuite(null, "Test Name", "Test Description", TestSuite.TestSuiteType.API);
        assertNull(testSuite.getId());
        
        // 模拟JPA的PrePersist调用
        testSuite.generateId();
        assertNotNull(testSuite.getId());
        assertFalse(testSuite.getId().isEmpty());
        
        System.out.println("Generated ID in constructor test: " + testSuite.getId());
    }
}