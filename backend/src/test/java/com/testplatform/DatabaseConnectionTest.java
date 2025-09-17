package com.testplatform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class DatabaseConnectionTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabaseConnection() {
        // 测试数据库连接
        Boolean result = jdbcTemplate.queryForObject("SELECT 1", Boolean.class);
        assertTrue(result, "数据库连接失败");
    }

    @Test
    public void testRequiredTablesExist() {
        // 检查必要的表是否存在
        checkTableExists("test_suites");
        checkTableExists("test_cases");
        checkTableExists("test_executions");
        checkTableExists("target_system_configs");
        checkTableExists("test_suite_test_cases");
    }

    @Test
    public void testRequiredColumnsExist() {
        // 检查test_cases表的test_steps字段
        checkColumnExists("test_cases", "test_steps");

        // 检查target_system_configs表的is_active字段
        checkColumnExists("target_system_configs", "is_active");
    }

    private void checkTableExists(String tableName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
        assertTrue(exists, "表 " + tableName + " 不存在");
    }

    private void checkColumnExists(String tableName, String columnName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.columns WHERE table_name = ? AND column_name = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
        assertTrue(exists, "表 " + tableName + " 的字段 " + columnName + " 不存在");
    }
}