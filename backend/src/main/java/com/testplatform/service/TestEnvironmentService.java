package com.testplatform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.testplatform.model.TestConnectionResult;
import com.testplatform.model.TestEnvironment;
import com.testplatform.repository.TestEnvironmentRepository;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 测试环境服务类
 * 处理测试环境的创建、更新、删除和查询
 */
@Service
@Transactional
public class TestEnvironmentService {

    @Autowired
    private TestEnvironmentRepository testEnvironmentRepository;

    /**
     * 创建测试环境
     */
    public TestEnvironment createEnvironment(TestEnvironment environment, String createdBy) {
        // 验证环境名称唯一性
        if (testEnvironmentRepository.findByName(environment.getName()).isPresent()) {
            throw new RuntimeException("环境名称已存在: " + environment.getName());
        }

        // 设置默认值
        environment.setId(java.util.UUID.randomUUID().toString());
        environment.setCreatedBy(createdBy);
        environment.setCreatedAt(LocalDateTime.now());
        environment.setUpdatedAt(LocalDateTime.now());
        
        // 如果isActive为null，设置为false
        System.out.println("DEBUG: Create - Received isActive value: " + environment.getIsActive());
        if (environment.getIsActive() == null) {
            environment.setIsActive(false);
            System.out.println("DEBUG: Create - Set isActive to false (was null)");
        } else {
            System.out.println("DEBUG: Create - Using provided isActive value: " + environment.getIsActive());
        }

        // 如果设置为活跃环境，先取消其他环境的活跃状态
        if (environment.getIsActive()) {
            deactivateAllEnvironmentsExcept(environment.getId());
        }

        return testEnvironmentRepository.save(environment);
    }

    /**
     * 更新测试环境
     */
    public TestEnvironment updateEnvironment(String id, TestEnvironment environment, String updatedBy) {
        TestEnvironment existingEnvironment = findById(id);
        if (existingEnvironment == null) {
            throw new RuntimeException("环境不存在: " + id);
        }

        // 验证环境名称唯一性（排除自己）
        Optional<TestEnvironment> nameConflict = testEnvironmentRepository.findByName(environment.getName());
        if (nameConflict.isPresent() && !nameConflict.get().getId().equals(id)) {
            throw new RuntimeException("环境名称已存在: " + environment.getName());
        }

        // 更新字段
        existingEnvironment.setName(environment.getName());
        existingEnvironment.setDescription(environment.getDescription());
        existingEnvironment.setApiBaseUrl(environment.getApiBaseUrl());
        existingEnvironment.setUiBaseUrl(environment.getUiBaseUrl());
        existingEnvironment.setDatabaseConfig(environment.getDatabaseConfig());
        existingEnvironment.setAuthConfig(environment.getAuthConfig());
        
        // 处理isActive字段，如果为null则保持原值
        System.out.println("DEBUG: Received isActive value: " + environment.getIsActive());
        if (environment.getIsActive() != null) {
            existingEnvironment.setIsActive(environment.getIsActive());
            System.out.println("DEBUG: Set isActive to: " + environment.getIsActive());
            
        // 如果设置为活跃环境，先取消其他环境的活跃状态
        if (environment.getIsActive()) {
            System.out.println("DEBUG: Deactivating all other environments");
            deactivateAllEnvironmentsExcept(id);
        }
        } else {
            System.out.println("DEBUG: isActive is null, keeping original value: " + existingEnvironment.getIsActive());
        }
        existingEnvironment.setUpdatedAt(LocalDateTime.now());

        return testEnvironmentRepository.save(existingEnvironment);
    }

    /**
     * 删除测试环境
     */
    public void deleteEnvironment(String id) {
        TestEnvironment environment = findById(id);
        if (environment == null) {
            throw new RuntimeException("环境不存在: " + id);
        }

        // 检查是否有关联的测试用例
        // TODO: 添加关联检查逻辑

        testEnvironmentRepository.delete(environment);
    }

    /**
     * 根据ID查找环境
     */
    public TestEnvironment findById(String id) {
        return testEnvironmentRepository.findById(id).orElse(null);
    }

    /**
     * 根据名称查找环境
     */
    public TestEnvironment findByName(String name) {
        return testEnvironmentRepository.findByName(name).orElse(null);
    }

    /**
     * 获取所有环境
     */
    public List<TestEnvironment> getAllEnvironments() {
        return testEnvironmentRepository.findAll();
    }

    /**
     * 获取活跃环境
     */
    public TestEnvironment getActiveEnvironment() {
        return testEnvironmentRepository.findByIsActiveTrue().orElse(null);
    }

    /**
     * 激活指定环境
     */
    public TestEnvironment activateEnvironment(String id) {
        TestEnvironment environment = findById(id);
        if (environment == null) {
            throw new RuntimeException("环境不存在: " + id);
        }

        // 先取消所有环境的活跃状态
        deactivateAllEnvironments();

        // 激活指定环境
        environment.setIsActive(true);
        environment.setUpdatedAt(LocalDateTime.now());

        return testEnvironmentRepository.save(environment);
    }

    /**
     * 取消所有环境的活跃状态
     */
    private void deactivateAllEnvironments() {
        Optional<TestEnvironment> activeEnvironment = testEnvironmentRepository.findByIsActiveTrue();
        if (activeEnvironment.isPresent()) {
            TestEnvironment env = activeEnvironment.get();
            env.setIsActive(false);
            env.setUpdatedAt(LocalDateTime.now());
            testEnvironmentRepository.save(env);
        }
    }

    /**
     * 取消所有环境的活跃状态（排除指定环境）
     */
    private void deactivateAllEnvironmentsExcept(String excludeId) {
        List<TestEnvironment> activeEnvironments = testEnvironmentRepository.findByIsActiveTrueOrderByCreatedAtDesc();
        for (TestEnvironment env : activeEnvironments) {
            if (!env.getId().equals(excludeId)) {
                env.setIsActive(false);
                env.setUpdatedAt(LocalDateTime.now());
                testEnvironmentRepository.save(env);
                System.out.println("DEBUG: Deactivated environment: " + env.getId());
            }
        }
    }

    /**
     * 验证环境配置（包括连通性测试）
     */
    public boolean validateEnvironmentConfig(TestEnvironment environment) {
        try {
            // 验证环境名称
            if (environment.getName() == null || environment.getName().trim().isEmpty()) {
                throw new RuntimeException("环境名称不能为空");
            }

            // 验证并测试API基础URL连通性
            if (environment.getApiBaseUrl() != null && !environment.getApiBaseUrl().isEmpty()) {
                java.net.URL url = new java.net.URL(environment.getApiBaseUrl());
                // 测试API连接
                String apiTestResult = testApiConnection(environment.getApiBaseUrl());
                if (!"success".equals(apiTestResult)) {
                    throw new RuntimeException("API连接测试失败: " + apiTestResult);
                }
            }

            // 验证并测试UI基础URL连通性
            if (environment.getUiBaseUrl() != null && !environment.getUiBaseUrl().isEmpty()) {
                java.net.URL url = new java.net.URL(environment.getUiBaseUrl());
                // 测试UI连接
                String uiTestResult = testUiConnection(environment.getUiBaseUrl());
                if (!"success".equals(uiTestResult)) {
                    throw new RuntimeException("UI连接测试失败: " + uiTestResult);
                }
            }

            // 验证并测试数据库配置
            if (environment.getDatabaseConfig() != null && !environment.getDatabaseConfig().trim().isEmpty()) {
                try {
                    // 验证JSON格式
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode dbConfig = mapper.readTree(environment.getDatabaseConfig());
                    
                    // 验证必需的数据库配置字段
                    if (!dbConfig.has("driver") || !dbConfig.has("url") || !dbConfig.has("username") || !dbConfig.has("password")) {
                        throw new RuntimeException("数据库配置缺少必需字段: driver, url, username, password");
                    }
                    
                    // 验证数据库驱动
                    String driver = dbConfig.get("driver").asText();
                    if (!"mysql".equals(driver) && !"postgresql".equals(driver) && !"oracle".equals(driver) && !"sqlserver".equals(driver)) {
                        throw new RuntimeException("不支持的数据库驱动: " + driver);
                    }
                    
                    // 验证URL格式
                    String url = dbConfig.get("url").asText();
                    if (!url.startsWith("jdbc:")) {
                        throw new RuntimeException("数据库URL格式无效，必须以jdbc:开头");
                    }
                    
                    // 如果有连接池配置，验证连接池参数
                    if (dbConfig.has("pool")) {
                        com.fasterxml.jackson.databind.JsonNode poolConfig = dbConfig.get("pool");
                        if (poolConfig.has("minSize")) {
                            int minSize = poolConfig.get("minSize").asInt();
                            if (minSize < 0) {
                                throw new RuntimeException("连接池最小连接数不能为负数");
                            }
                        }
                        if (poolConfig.has("maxSize")) {
                            int maxSize = poolConfig.get("maxSize").asInt();
                            if (maxSize <= 0) {
                                throw new RuntimeException("连接池最大连接数必须大于0");
                            }
                        }
                        if (poolConfig.has("connectionTimeout")) {
                            int connectionTimeout = poolConfig.get("connectionTimeout").asInt();
                            if (connectionTimeout <= 0) {
                                throw new RuntimeException("连接超时时间必须大于0");
                            }
                        }
                        if (poolConfig.has("idleTimeout")) {
                            int idleTimeout = poolConfig.get("idleTimeout").asInt();
                            if (idleTimeout <= 0) {
                                throw new RuntimeException("空闲超时时间必须大于0");
                            }
                        }
                    }
                    
                    // 测试数据库连接
                    String dbTestResult = testDatabaseConnection(environment.getDatabaseConfig());
                    if (!"success".equals(dbTestResult)) {
                        throw new RuntimeException("数据库连接测试失败: " + dbTestResult);
                    }
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException("数据库配置JSON格式无效: " + e.getMessage());
                }
            }

            // 验证并测试认证配置
            if (environment.getAuthConfig() != null && !environment.getAuthConfig().trim().isEmpty()) {
                try {
                    // 验证JSON格式
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode authConfig = mapper.readTree(environment.getAuthConfig());
                    
                    // 验证认证类型
                    if (!authConfig.has("type")) {
                        throw new RuntimeException("认证配置缺少类型字段");
                    }
                    
                    String authType = authConfig.get("type").asText().toLowerCase();
                    if ("jwt".equals(authType)) {
                        // JWT认证验证必需字段
                        if (!authConfig.has("loginUrl")) {
                            throw new RuntimeException("JWT认证配置缺少loginUrl字段");
                        }
                        
                        if (!authConfig.has("credentials")) {
                            throw new RuntimeException("JWT认证配置缺少credentials字段");
                        }
                        
                        com.fasterxml.jackson.databind.JsonNode credentials = authConfig.get("credentials");
                        if (!credentials.has("username") || !credentials.has("password")) {
                            throw new RuntimeException("JWT认证配置的credentials缺少username或password字段");
                        }
                        
                        if (!authConfig.has("tokenField")) {
                            throw new RuntimeException("JWT认证配置缺少tokenField字段");
                        }
                        
                        if (!authConfig.has("headerName")) {
                            throw new RuntimeException("JWT认证配置缺少headerName字段");
                        }
                        
                        if (!authConfig.has("headerFormat")) {
                            throw new RuntimeException("JWT认证配置缺少headerFormat字段");
                        }
                        
                        // 测试JWT认证连接
                        String authTestResult = testJwtAuthConnection(authConfig);
                        if (!"success".equals(authTestResult)) {
                            throw new RuntimeException("JWT认证连接测试失败: " + authTestResult);
                        }
                    } else if ("basic".equals(authType)) {
                        // BASIC认证验证必需字段
                        if (!authConfig.has("credentials")) {
                            throw new RuntimeException("BASIC认证配置缺少credentials字段");
                        }
                        
                        com.fasterxml.jackson.databind.JsonNode credentials = authConfig.get("credentials");
                        if (!credentials.has("username") || !credentials.has("password")) {
                            throw new RuntimeException("BASIC认证配置的credentials缺少username或password字段");
                        }
                        
                        // 测试Basic认证连接（如果需要）
                        // String authTestResult = testBasicAuthConnection(authConfig);
                        // if (!"success".equals(authTestResult)) {
                        //     throw new RuntimeException("BASIC认证连接测试失败: " + authTestResult);
                        // }
                    } else if ("api_key".equals(authType)) {
                        // API_KEY认证验证必需字段
                        if (!authConfig.has("keyName") || !authConfig.has("keyValue")) {
                            throw new RuntimeException("API_KEY认证配置缺少keyName或keyValue字段");
                        }
                    } else {
                        throw new RuntimeException("不支持的认证类型: " + authType);
                    }
                } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                    throw new RuntimeException("认证配置JSON格式无效: " + e.getMessage());
                }
            }

            return true;
        } catch (Exception e) {
            // 记录验证错误日志
            System.err.println("环境配置验证失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 测试环境连接
     */
    public boolean testEnvironmentConnection(String id) {
        TestEnvironment environment = findById(id);
        if (environment == null) {
            return false;
        }

        try {
            // 测试API连接
            if (environment.getApiBaseUrl() != null && !environment.getApiBaseUrl().isEmpty()) {
                java.net.URL url = new java.net.URL(environment.getApiBaseUrl());
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                int responseCode = connection.getResponseCode();
                connection.disconnect();
                
                // 认为2xx和3xx状态码都是有效的
                if (!(responseCode >= 200 && responseCode < 400)) {
                    System.err.println("API连接测试失败，响应码: " + responseCode);
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("API连接测试异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 全面测试环境配置连接性
     * 包括API、UI、数据库和认证配置的连接测试
     */
    public TestConnectionResult testFullEnvironmentConnection(String id) {
        TestEnvironment environment = findById(id);
        if (environment == null) {
            return new TestConnectionResult(false, "环境不存在");
        }

        TestConnectionResult result = new TestConnectionResult(true, "连接测试成功");
        
        try {
            // 测试API连接
            if (environment.getApiBaseUrl() != null && !environment.getApiBaseUrl().isEmpty()) {
                String apiTestResult = testApiConnection(environment.getApiBaseUrl());
                if (!apiTestResult.equals("success")) {
                    result.setSuccess(false);
                    result.setMessage("API连接测试失败: " + apiTestResult);
                    return result;
                }
            }

            // 测试UI连接
            if (environment.getUiBaseUrl() != null && !environment.getUiBaseUrl().isEmpty()) {
                String uiTestResult = testUiConnection(environment.getUiBaseUrl());
                if (!uiTestResult.equals("success")) {
                    result.setSuccess(false);
                    result.setMessage("UI连接测试失败: " + uiTestResult);
                    return result;
                }
            }

            // 测试数据库连接
            if (environment.getDatabaseConfig() != null && !environment.getDatabaseConfig().trim().isEmpty()) {
                String dbTestResult = testDatabaseConnection(environment.getDatabaseConfig());
                if (!dbTestResult.equals("success")) {
                    result.setSuccess(false);
                    result.setMessage("数据库连接测试失败: " + dbTestResult);
                    return result;
                }
            }

            // 测试认证配置
            if (environment.getAuthConfig() != null && !environment.getAuthConfig().trim().isEmpty()) {
                String authTestResult = testAuthConnection(environment.getAuthConfig());
                if (!authTestResult.equals("success")) {
                    result.setSuccess(false);
                    result.setMessage("认证连接测试失败: " + authTestResult);
                    return result;
                }
            }

            return result;
        } catch (Exception e) {
            System.err.println("环境连接测试异常: " + e.getMessage());
            return new TestConnectionResult(false, "连接测试异常: " + e.getMessage());
        }
    }

    /**
     * 测试API连接
     */
    private String testApiConnection(String apiUrl) {
        try {
            java.net.URL url = new java.net.URL(apiUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            // 认为2xx和3xx状态码都是有效的
            if (responseCode >= 200 && responseCode < 400) {
                return "success";
            } else {
                return "响应码: " + responseCode;
            }
        } catch (Exception e) {
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 测试UI连接
     */
    private String testUiConnection(String uiUrl) {
        try {
            java.net.URL url = new java.net.URL(uiUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            // 认为2xx和3xx状态码都是有效的
            if (responseCode >= 200 && responseCode < 400) {
                return "success";
            } else {
                return "响应码: " + responseCode;
            }
        } catch (Exception e) {
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 测试数据库连接
     */
    private String testDatabaseConnection(String databaseConfig) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode dbConfig = mapper.readTree(databaseConfig);
            
            String driver = dbConfig.get("driver").asText();
            String url = dbConfig.get("url").asText();
            String username = dbConfig.get("username").asText();
            String password = dbConfig.get("password").asText();
            
            // 加载数据库驱动
            Class.forName(getDriverClassName(driver));
            
            // 尝试建立连接
            java.sql.Connection connection = java.sql.DriverManager.getConnection(url, username, password);
            boolean isValid = connection.isValid(5); // 5秒超时
            connection.close();
            
            return isValid ? "success" : "连接无效";
        } catch (Exception e) {
            e.printStackTrace();
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 根据驱动类型获取驱动类名
     */
    private String getDriverClassName(String driver) {
        switch (driver.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "oracle":
                return "oracle.jdbc.driver.OracleDriver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            default:
                throw new IllegalArgumentException("不支持的数据库驱动: " + driver);
        }
    }

    /**
     * 测试认证连接
     */
    private String testAuthConnection(String authConfig) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode authConfigNode = mapper.readTree(authConfig);
            
            String authType = authConfigNode.get("type").asText().toLowerCase();
            
            if ("jwt".equals(authType)) {
                return testJwtAuthConnection(authConfigNode);
            } else if ("basic".equals(authType)) {
                return testBasicAuthConnection(authConfigNode);
            } else if ("api_key".equals(authType)) {
                return "API_KEY认证无需连接测试";
            } else {
                return "不支持的认证类型: " + authType;
            }
        } catch (Exception e) {
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 测试JWT认证连接
     */
    private String testJwtAuthConnection(com.fasterxml.jackson.databind.JsonNode authConfig) {
        try {
            String loginUrl = authConfig.get("loginUrl").asText();
            com.fasterxml.jackson.databind.JsonNode credentials = authConfig.get("credentials");
            String username = credentials.get("username").asText();
            String password = credentials.get("password").asText();
            
            // 创建登录请求
            java.net.URL url = new java.net.URL(loginUrl);
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            
            // 发送登录凭据
            String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);
            try (java.io.OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            int responseCode = connection.getResponseCode();
            connection.disconnect();
            
            // 2xx状态码表示登录成功
            if (responseCode >= 200 && responseCode < 300) {
                return "success";
            } else {
                return "登录失败，响应码: " + responseCode;
            }
        } catch (Exception e) {
            return "异常: " + e.getMessage();
        }
    }

    /**
     * 测试Basic认证连接
     */
    private String testBasicAuthConnection(com.fasterxml.jackson.databind.JsonNode authConfig) {
        try {
            com.fasterxml.jackson.databind.JsonNode credentials = authConfig.get("credentials");
            String username = credentials.get("username").asText();
            String password = credentials.get("password").asText();
            
            // 对于Basic认证，我们假设有一个测试端点
            // 这里只是一个示例，实际应用中需要根据具体API调整
            return "Basic认证测试需要具体端点信息";
        } catch (Exception e) {
            return "异常: " + e.getMessage();
        }
    }
}
