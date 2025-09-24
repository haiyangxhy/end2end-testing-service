package com.testplatform.service;

import com.testplatform.model.TestEnvironment;
import com.testplatform.repository.TestEnvironmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 验证环境配置
     */
    public boolean validateEnvironmentConfig(TestEnvironment environment) {
        try {
            // 验证API基础URL
            if (environment.getApiBaseUrl() != null && !environment.getApiBaseUrl().isEmpty()) {
                java.net.URL url = new java.net.URL(environment.getApiBaseUrl());
                // 可以添加更多验证逻辑，如ping测试
            }

            // 验证UI基础URL
            if (environment.getUiBaseUrl() != null && !environment.getUiBaseUrl().isEmpty()) {
                java.net.URL url = new java.net.URL(environment.getUiBaseUrl());
            }

            // 验证数据库配置
            if (environment.getDatabaseConfig() != null && !environment.getDatabaseConfig().trim().isEmpty()) {
                try {
                    // 验证JSON格式
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.readTree(environment.getDatabaseConfig());
                } catch (Exception e) {
                    throw new RuntimeException("数据库配置JSON格式无效: " + e.getMessage());
                }
            }

            // 验证认证配置
            if (environment.getAuthConfig() != null && !environment.getAuthConfig().trim().isEmpty()) {
                try {
                    // 验证JSON格式
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    mapper.readTree(environment.getAuthConfig());
                } catch (Exception e) {
                    throw new RuntimeException("认证配置JSON格式无效: " + e.getMessage());
                }
            }

            return true;
        } catch (Exception e) {
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
                return responseCode >= 200 && responseCode < 400;
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
