package com.testplatform.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TestConnectionResultTest {

    @Test
    void testDefaultConstructor() {
        TestConnectionResult result = new TestConnectionResult();
        assertNotNull(result);
    }

    @Test
    void testConstructorWithParameters() {
        TestConnectionResult result = new TestConnectionResult(true, "测试成功");
        assertTrue(result.isSuccess());
        assertEquals("测试成功", result.getMessage());
    }

    @Test
    void testGettersAndSetters() {
        TestConnectionResult result = new TestConnectionResult();
        
        // Test success
        result.setSuccess(true);
        assertTrue(result.isSuccess());
        
        result.setSuccess(false);
        assertFalse(result.isSuccess());
        
        // Test message
        result.setMessage("测试消息");
        assertEquals("测试消息", result.getMessage());
    }
}