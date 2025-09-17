package com.testplatform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class DatabaseSchemaTest {

    @Test
    public void testDatabaseConnectionAndTables() throws Exception {
        // 获取数据库连接信息
        String url = "jdbc:postgresql://localhost:5432/testplatform";
        String username = "postgres";
        String password = "root";

        // 建立数据库连接
        try (Connection connection = DriverManager.getConnection(url, username, password)) {
            // 检查连接是否成功
            assertTrue(connection.isValid(5), "数据库连接失败");

            // 检查必要的表是否存在
            Statement statement = connection.createStatement();
            
            // 检查test_suites表
            ResultSet rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_suites')");
            rs.next();
            assertTrue(rs.getBoolean(1), "test_suites表不存在");
            
            // 检查test_cases表
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_cases')");
            rs.next();
            assertTrue(rs.getBoolean(1), "test_cases表不存在");
            
            // 检查test_executions表
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_executions')");
            rs.next();
            assertTrue(rs.getBoolean(1), "test_executions表不存在");
            
            // 检查target_system_configs表
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'target_system_configs')");
            rs.next();
            assertTrue(rs.getBoolean(1), "target_system_configs表不存在");
            
            // 检查test_suite_test_cases表
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'test_suite_test_cases')");
            rs.next();
            assertTrue(rs.getBoolean(1), "test_suite_test_cases表不存在");
            
            // 检查test_cases表是否有test_steps字段
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'test_cases' AND column_name = 'test_steps')");
            rs.next();
            assertTrue(rs.getBoolean(1), "test_cases表缺少test_steps字段");
            
            // 检查target_system_configs表是否有is_active字段
            rs = statement.executeQuery(
                "SELECT EXISTS (SELECT FROM information_schema.columns WHERE table_name = 'target_system_configs' AND column_name = 'is_active')");
            rs.next();
            assertTrue(rs.getBoolean(1), "target_system_configs表缺少is_active字段");
        }
    }
}