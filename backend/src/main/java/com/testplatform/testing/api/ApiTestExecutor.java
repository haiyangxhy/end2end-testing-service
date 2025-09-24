package com.testplatform.testing.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestCase;
import com.testplatform.model.TestEnvironment;
import com.testplatform.testing.TestExecutionResult;
import com.testplatform.testing.TestExecutor;
import com.testplatform.testing.VariableManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
public class ApiTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ApiTestExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;
    
    @Autowired
    private VariableManager variableManager;
    
    public ApiTestExecutor() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }
    
    @Override
    public TestExecutionResult execute(TestCase testCase, TestEnvironment environment) {
        return executeWithRetry(testCase, environment, 0);
    }
    
    /**
     * 带重试机制的测试执行
     */
    public TestExecutionResult executeWithRetry(TestCase testCase, TestEnvironment environment, int retryCount) {
        long startTime = System.currentTimeMillis();
        int maxRetries = 3; // 最大重试次数
        
        try {
            // 解析测试用例配置
            String testCaseConfig = testCase.getConfig();
            if (testCaseConfig == null || testCaseConfig.trim().isEmpty()) {
                return new TestExecutionResult(false, "测试用例配置为空", System.currentTimeMillis() - startTime);
            }
            
            // 解析JSON配置
            ApiTestConfig apiConfig = parseConfig(testCaseConfig);
            
            // 构建完整URL并替换变量
            String baseUrl = environment != null ? environment.getApiBaseUrl() : "";
            String pathWithVariablesReplaced = variableManager.replaceVariables(apiConfig.getUrl());
            String fullUrl = buildFullUrl(baseUrl, pathWithVariablesReplaced);
            
            // 替换请求体中的变量
            String bodyWithVariablesReplaced = variableManager.replaceVariables(apiConfig.getBody());
            
            // 替换请求头中的变量
            Map<String, String> headersWithVariablesReplaced = new HashMap<>();
            for (Map.Entry<String, String> entry : apiConfig.getHeaders().entrySet()) {
                headersWithVariablesReplaced.put(entry.getKey(), variableManager.replaceVariables(entry.getValue()));
            }
            
            // 创建HTTP请求
            HttpRequest request = buildRequest(fullUrl, apiConfig.getMethod(), headersWithVariablesReplaced, bodyWithVariablesReplaced, apiConfig.getTimeout());
            
            // 执行HTTP请求（带超时）
            logger.info("执行API请求: {} {} (重试次数: {})", apiConfig.getMethod(), fullUrl, retryCount);
            HttpResponse<String> response = executeRequestWithTimeout(request, apiConfig.getTimeout());
            
            // 从响应中提取变量
            if (apiConfig.getExtractors() != null && !apiConfig.getExtractors().isEmpty()) {
                variableManager.extractVariablesFromJson(response.body(), apiConfig.getExtractors());
            }
            
            // 验证断言
            boolean assertionsPassed = validateAssertions(response, apiConfig.getAssertions());
            
            if (!assertionsPassed) {
                if (retryCount < maxRetries) {
                    logger.warn("断言验证失败，准备重试 (第{}次)", retryCount + 1);
                    Thread.sleep(1000 * (retryCount + 1)); // 递增延迟
                    return executeWithRetry(testCase, environment, retryCount + 1);
                }
                return new TestExecutionResult(false, "断言验证失败", System.currentTimeMillis() - startTime);
            }
            
            return new TestExecutionResult(true, "API测试执行成功", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            if (retryCount < maxRetries && isRetryableException(e)) {
                logger.warn("API测试执行异常，准备重试 (第{}次): {}", retryCount + 1, e.getMessage());
                try {
                    Thread.sleep(1000 * (retryCount + 1)); // 递增延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return executeWithRetry(testCase, environment, retryCount + 1);
            }
            
            logger.error("API测试执行失败", e);
            return new TestExecutionResult(false, "API测试执行异常: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof java.net.ConnectException ||
               e instanceof java.net.SocketTimeoutException ||
               e instanceof java.util.concurrent.TimeoutException ||
               e.getMessage().contains("timeout") ||
               e.getMessage().contains("connection");
    }
    
    /**
     * 带超时的请求执行
     */
    private HttpResponse<String> executeRequestWithTimeout(HttpRequest request, int timeoutMs) throws Exception {
        CompletableFuture<HttpResponse<String>> future = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        
        try {
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            future.cancel(true);
            throw new java.util.concurrent.TimeoutException("请求超时: " + timeoutMs + "ms");
        }
    }
    
    private ApiTestConfig parseConfig(String configJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(configJson);
        
        ApiTestConfig config = new ApiTestConfig();
        config.setMethod(rootNode.has("method") ? rootNode.get("method").asText() : "GET");
        config.setUrl(rootNode.has("url") ? rootNode.get("url").asText() : "");
        config.setTimeout(rootNode.has("timeout") ? rootNode.get("timeout").asInt(10000) : 10000);
        
        // 解析请求头
        Map<String, String> headers = new HashMap<>();
        if (rootNode.has("headers")) {
            JsonNode headersNode = rootNode.get("headers");
            headersNode.fields().forEachRemaining(entry -> {
                headers.put(entry.getKey(), entry.getValue().asText());
            });
        }
        config.setHeaders(headers);
        
        // 解析请求体
        if (rootNode.has("body")) {
            config.setBody(rootNode.get("body").asText());
        }
        
        // 解析断言
        if (rootNode.has("assertions")) {
            config.setAssertions(rootNode.get("assertions"));
        }
        
        // 解析变量提取器
        if (rootNode.has("extractors")) {
            config.setExtractors(rootNode.get("extractors"));
        }
        
        return config;
    }
    
    private String buildFullUrl(String baseUrl, String path) {
        // 移除baseUrl末尾可能的斜杠和path开头可能的斜杠
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + "/" + path;
    }
    
    private HttpRequest buildRequest(String url, String method, Map<String, String> headers, String body, int timeoutMs) throws IOException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs));
        
        // 添加请求头
        if (headers != null) {
            headers.forEach(requestBuilder::header);
        }
        
        // 设置请求方法和请求体
        switch (method.toUpperCase()) {
            case "GET":
                requestBuilder.GET();
                break;
            case "POST":
                requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;
            case "PUT":
                requestBuilder.PUT(HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;
            case "DELETE":
                requestBuilder.DELETE();
                break;
            case "PATCH":
                requestBuilder.method("PATCH", HttpRequest.BodyPublishers.ofString(body != null ? body : ""));
                break;
            case "HEAD":
                requestBuilder.method("HEAD", HttpRequest.BodyPublishers.noBody());
                break;
            case "OPTIONS":
                requestBuilder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
                break;
            default:
                requestBuilder.GET();
        }
        
        return requestBuilder.build();
    }
    
    private boolean validateAssertions(HttpResponse<String> response, JsonNode assertions) {
        // 如果没有断言，默认返回成功
        if (assertions == null || !assertions.isArray() || assertions.isEmpty()) {
            return true;
        }
        
        // 简单的断言验证逻辑
        for (JsonNode assertion : assertions) {
            String type = assertion.get("type").asText();
            Object expected = getValueFromNode(assertion.get("expected"));
            
            switch (type) {
                case "statusCode":
                    int actualStatusCode = response.statusCode();
                    if (expected instanceof Integer && !expected.equals(actualStatusCode)) {
                        logger.error("状态码断言失败: 期望={}, 实际={}", expected, actualStatusCode);
                        return false;
                    }
                    break;
                case "responseTime":
                    // 响应时间断言需要在调用层处理
                    break;
                // 可以添加更多断言类型
            }
        }
        
        return true;
    }
    
    private Object getValueFromNode(JsonNode node) {
        if (node.isInt()) return node.asInt();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isDouble()) return node.asDouble();
        return node.asText();
    }
    
    // API测试配置类
    private static class ApiTestConfig {
        private String method;
        private String url;
        private Map<String, String> headers;
        private String body;
        private int timeout;
        private JsonNode assertions;
        private JsonNode extractors;
        
        // Getters and Setters
        public String getMethod() { return method; }
        public void setMethod(String method) { this.method = method; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public Map<String, String> getHeaders() { return headers; }
        public void setHeaders(Map<String, String> headers) { this.headers = headers; }
        public String getBody() { return body; }
        public void setBody(String body) { this.body = body; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public JsonNode getAssertions() { return assertions; }
        public void setAssertions(JsonNode assertions) { this.assertions = assertions; }
        public JsonNode getExtractors() { return extractors; }
        public void setExtractors(JsonNode extractors) { this.extractors = extractors; }
    }
}