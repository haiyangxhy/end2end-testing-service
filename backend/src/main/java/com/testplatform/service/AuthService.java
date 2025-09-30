package com.testplatform.service;

import com.testplatform.model.TestEnvironment;
import com.testplatform.model.AuthConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 执行认证流程
     */
    public AuthResult authenticate(TestEnvironment environment) {
        try {
            // 解析认证配置
            AuthConfig authConfig = parseAuthConfig(environment.getAuthConfig());
            if (authConfig == null) {
                logger.warn("环境 {} 没有配置认证信息", environment.getName());
                return new AuthResult(false, "环境未配置认证信息", null, null, null);
            }
            
            // 根据认证类型执行不同的认证流程
            switch (authConfig.getType()) {
                case "jwt":
                    return authenticateJWT(environment, authConfig);
                case "basic":
                    return authenticateBasic(environment, authConfig);
                case "apiKey":
                    return authenticateApiKey(environment, authConfig);
                case "oauth2":
                    return authenticateOAuth2(environment, authConfig);
                case "none":
                    return new AuthResult(true, "无需认证", null, null, null);
                default:
                    return new AuthResult(false, "不支持的认证类型: " + authConfig.getType(), null, null, null);
            }
        } catch (Exception e) {
            logger.error("认证失败", e);
            return new AuthResult(false, "认证失败: " + e.getMessage(), null, null, null);
        }
    }
    
    /**
     * JWT认证
     */
    private AuthResult authenticateJWT(TestEnvironment environment, AuthConfig authConfig) {
        try {
            // 构建登录请求
            String loginUrl = environment.getApiBaseUrl() + authConfig.getLoginUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> loginRequest = new HashMap<>();
            loginRequest.put("username", authConfig.getCredentials().getUsername());
            loginRequest.put("password", authConfig.getCredentials().getPassword());
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(loginRequest, headers);
            
            // 发送登录请求
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String token = (String) responseBody.get(authConfig.getTokenField());
                String refreshToken = (String) responseBody.get(authConfig.getRefreshTokenField());
                String tokenVersion = (String) responseBody.get(authConfig.getTokenVersionField());
                
                logger.info("JWT认证成功，获取到token");
                return new AuthResult(true, "认证成功", token, refreshToken, tokenVersion);
            } else {
                logger.error("JWT认证失败，状态码: {}", response.getStatusCode());
                return new AuthResult(false, "认证失败，状态码: " + response.getStatusCode(), null, null, null);
            }
        } catch (Exception e) {
            logger.error("JWT认证异常", e);
            return new AuthResult(false, "JWT认证异常: " + e.getMessage(), null, null, null);
        }
    }
    
    /**
     * Basic认证
     */
    private AuthResult authenticateBasic(TestEnvironment environment, AuthConfig authConfig) {
        // Basic认证通常不需要单独的登录步骤，直接返回成功
        logger.info("Basic认证配置完成");
        return new AuthResult(true, "Basic认证配置完成", null, null, null);
    }
    
    /**
     * API Key认证
     */
    private AuthResult authenticateApiKey(TestEnvironment environment, AuthConfig authConfig) {
        // API Key认证不需要登录步骤，直接返回成功
        logger.info("API Key认证配置完成");
        return new AuthResult(true, "API Key认证配置完成", null, null, null);
    }
    
    /**
     * OAuth2认证
     */
    private AuthResult authenticateOAuth2(TestEnvironment environment, AuthConfig authConfig) {
        // TODO: 实现OAuth2认证流程
        logger.info("OAuth2认证暂未实现");
        return new AuthResult(false, "OAuth2认证暂未实现", null, null, null);
    }
    
    /**
     * 刷新Token
     */
    public AuthResult refreshToken(TestEnvironment environment, String refreshToken, String tokenVersion) {
        try {
            AuthConfig authConfig = parseAuthConfig(environment.getAuthConfig());
            if (authConfig == null || authConfig.getRefreshUrl() == null) {
                return new AuthResult(false, "未配置刷新Token的URL", null, null, null);
            }
            
            String refreshUrl = environment.getApiBaseUrl() + authConfig.getRefreshUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> refreshRequest = new HashMap<>();
            if (authConfig.getRefreshParams() != null) {
                for (Map.Entry<String, String> entry : authConfig.getRefreshParams().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    // 替换变量
                    value = value.replace("{refreshToken}", refreshToken);
                    value = value.replace("{tokenVersion}", tokenVersion);
                    refreshRequest.put(key, value);
                }
            }
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(refreshRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(refreshUrl, request, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                String newToken = (String) responseBody.get(authConfig.getTokenField());
                String newRefreshToken = (String) responseBody.get(authConfig.getRefreshTokenField());
                String newTokenVersion = (String) responseBody.get(authConfig.getTokenVersionField());
                
                logger.info("Token刷新成功");
                return new AuthResult(true, "Token刷新成功", newToken, newRefreshToken, newTokenVersion);
            } else {
                logger.error("Token刷新失败，状态码: {}", response.getStatusCode());
                return new AuthResult(false, "Token刷新失败，状态码: " + response.getStatusCode(), null, null, null);
            }
        } catch (Exception e) {
            logger.error("Token刷新异常", e);
            return new AuthResult(false, "Token刷新异常: " + e.getMessage(), null, null, null);
        }
    }
    
    /**
     * 解析认证配置
     */
    private AuthConfig parseAuthConfig(String authConfigJson) {
        try {
            if (authConfigJson == null || authConfigJson.trim().isEmpty()) {
                return null;
            }
            return objectMapper.readValue(authConfigJson, AuthConfig.class);
        } catch (Exception e) {
            logger.error("解析认证配置失败", e);
            return null;
        }
    }
    
    /**
     * 认证结果
     */
    public static class AuthResult {
        private boolean success;
        private String message;
        private String token;
        private String refreshToken;
        private String tokenVersion;
        
        public AuthResult(boolean success, String message, String token, String refreshToken, String tokenVersion) {
            this.success = success;
            this.message = message;
            this.token = token;
            this.refreshToken = refreshToken;
            this.tokenVersion = tokenVersion;
        }
        
        // Getters and Setters
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getTokenVersion() { return tokenVersion; }
        public void setTokenVersion(String tokenVersion) { this.tokenVersion = tokenVersion; }
    }
}
