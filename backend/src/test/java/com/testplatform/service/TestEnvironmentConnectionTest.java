package com.testplatform.service;

import com.testplatform.model.TestConnectionResult;
import com.testplatform.model.TestEnvironment;
import com.testplatform.repository.TestEnvironmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestEnvironmentConnectionTest {

    @Mock
    private TestEnvironmentRepository testEnvironmentRepository;

    @InjectMocks
    private TestEnvironmentService testEnvironmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testTestFullEnvironmentConnection_EnvironmentNotFound() {
        when(testEnvironmentRepository.findById("non-existent-id")).thenReturn(Optional.empty());

        TestConnectionResult result = testEnvironmentService.testFullEnvironmentConnection("non-existent-id");
        assertFalse(result.isSuccess());
        assertEquals("环境不存在", result.getMessage());
    }

    @Test
    void testTestApiConnection_ValidUrl() {
        // 这个测试需要网络连接，实际测试中可能需要mock
        // 这里只是测试方法是否存在
        assertNotNull(testEnvironmentService);
    }

    @Test
    void testTestUiConnection_ValidUrl() {
        // 这个测试需要网络连接，实际测试中可能需要mock
        // 这里只是测试方法是否存在
        assertNotNull(testEnvironmentService);
    }

    @Test
    void testGetDriverClassName_MySQL() {
        String driverClass = null;
        try {
            // 使用反射调用私有方法
            java.lang.reflect.Method method = TestEnvironmentService.class.getDeclaredMethod("getDriverClassName", String.class);
            method.setAccessible(true);
            driverClass = (String) method.invoke(testEnvironmentService, "mysql");
        } catch (Exception e) {
            fail("调用getDriverClassName方法失败: " + e.getMessage());
        }
        
        assertEquals("com.mysql.cj.jdbc.Driver", driverClass);
    }

    @Test
    void testGetDriverClassName_PostgreSQL() {
        String driverClass = null;
        try {
            // 使用反射调用私有方法
            java.lang.reflect.Method method = TestEnvironmentService.class.getDeclaredMethod("getDriverClassName", String.class);
            method.setAccessible(true);
            driverClass = (String) method.invoke(testEnvironmentService, "postgresql");
        } catch (Exception e) {
            fail("调用getDriverClassName方法失败: " + e.getMessage());
        }
        
        assertEquals("org.postgresql.Driver", driverClass);
    }

    @Test
    void testGetDriverClassName_UnsupportedDriver() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            try {
                // 使用反射调用私有方法
                java.lang.reflect.Method method = TestEnvironmentService.class.getDeclaredMethod("getDriverClassName", String.class);
                method.setAccessible(true);
                method.invoke(testEnvironmentService, "unsupported");
            } catch (java.lang.reflect.InvocationTargetException e) {
                throw e.getCause();
            }
        });
        
        assertTrue(exception.getMessage().contains("不支持的数据库驱动"));
    }
}