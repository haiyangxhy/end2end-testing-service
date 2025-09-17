package com.testplatform.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseValidator {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void validateSchema() {
        System.out.println("开始验证数据库表结构...");

        // 检查必要的表是否存在
        checkTableExists("test_suites");
        checkTableExists("test_cases");
        checkTableExists("test_executions");
        checkTableExists("target_system_configs");
        checkTableExists("test_suite_test_cases");

        // 检查test_cases表的字段
        checkColumnExists("test_cases", "test_steps");

        // 检查target_system_configs表的字段
        checkColumnExists("target_system_configs", "is_active");

        System.out.println("数据库表结构验证完成。");
    }

    private void checkTableExists(String tableName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName);
        if (exists) {
            System.out.println("表 " + tableName + " 存在。");
        } else {
            System.out.println("警告: 表 " + tableName + " 不存在。");
        }
    }

    private void checkColumnExists(String tableName, String columnName) {
        String sql = "SELECT EXISTS (SELECT FROM information_schema.columns WHERE table_name = ? AND column_name = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, tableName, columnName);
        if (exists) {
            System.out.println("表 " + tableName + " 的字段 " + columnName + " 存在。");
        } else {
            System.out.println("警告: 表 " + tableName + " 的字段 " + columnName + " 不存在。");
        }
    }
}