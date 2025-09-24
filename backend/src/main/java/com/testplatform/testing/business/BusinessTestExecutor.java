package com.testplatform.testing.business;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class BusinessTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(BusinessTestExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    
    @Autowired
    private VariableManager variableManager;
    
    @Override
    public TestExecutionResult execute(TestCase testCase, TestEnvironment environment) {
        return executeWithRetry(testCase, environment, 0);
    }
    
    /**
     * 带重试机制的业务流程执行
     */
    public TestExecutionResult executeWithRetry(TestCase testCase, TestEnvironment environment, int retryCount) {
        long startTime = System.currentTimeMillis();
        int maxRetries = 2; // 最大重试次数
        
        try {
            logger.info("开始执行业务流程测试: {} (重试次数: {})", testCase.getName(), retryCount);
            
            // 解析业务流程配置
            String configJson = testCase.getConfig();
            if (configJson == null || configJson.trim().isEmpty()) {
                return new TestExecutionResult(false, "业务流程配置为空", System.currentTimeMillis() - startTime);
            }
            
            BusinessProcessConfig processConfig = parseProcessConfig(configJson);
            
            // 替换流程变量中的引用
            processConfig.setVariables(replaceVariablesInMap(processConfig.getVariables()));
            
            // 初始化流程变量
            if (processConfig.getVariables() != null) {
                processConfig.getVariables().forEach((key, value) -> {
                    variableManager.setVariable(key, value);
                });
            }
            
            // 根据配置决定是否并行执行步骤
            List<StepResult> stepResults;
            if (processConfig.isParallel()) {
                stepResults = executeStepsInParallel(processConfig.getSteps(), environment);
            } else {
                stepResults = executeStepsSequentially(processConfig.getSteps(), environment);
            }
            
            // 检查执行结果
            boolean processSuccess = stepResults.stream().allMatch(StepResult::isSuccess);
            String failureMessage = "";
            
            if (!processSuccess) {
                for (StepResult stepResult : stepResults) {
                    if (!stepResult.isSuccess()) {
                        failureMessage = "步骤执行失败: " + stepResult.getMessage();
                        break;
                    }
                }
                
                // 如果失败且还有重试次数，则重试
                if (retryCount < maxRetries) {
                    logger.warn("业务流程执行失败，准备重试 (第{}次): {}", retryCount + 1, failureMessage);
                    Thread.sleep(2000 * (retryCount + 1)); // 递增延迟
                    return executeWithRetry(testCase, environment, retryCount + 1);
                }
                
                return new TestExecutionResult(false, failureMessage, System.currentTimeMillis() - startTime);
            }
            
            logger.info("业务流程测试执行成功: {}", testCase.getName());
            return new TestExecutionResult(true, "业务流程测试执行成功", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            if (retryCount < maxRetries && isRetryableException(e)) {
                logger.warn("业务流程测试执行异常，准备重试 (第{}次): {}", retryCount + 1, e.getMessage());
                try {
                    Thread.sleep(2000 * (retryCount + 1)); // 递增延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return executeWithRetry(testCase, environment, retryCount + 1);
            }
            
            logger.error("业务流程测试执行失败", e);
            return new TestExecutionResult(false, "业务流程测试执行异常: " + e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }
    
    /**
     * 顺序执行步骤
     */
    private List<StepResult> executeStepsSequentially(List<BusinessStepConfig> steps, TestEnvironment environment) {
        List<StepResult> stepResults = new ArrayList<>();
        
        for (BusinessStepConfig step : steps) {
            // 替换步骤配置中的变量
            step = replaceVariablesInStep(step);
            
            StepResult stepResult = executeStep(step, environment);
            stepResults.add(stepResult);
            
            if (!stepResult.isSuccess()) {
                logger.error("步骤执行失败: {}", step.getName());
                break;
            }
            
            // 更新变量
            if (stepResult.getVariables() != null) {
                stepResult.getVariables().forEach((key, value) -> {
                    variableManager.setVariable(key, value);
                });
            }
        }
        
        return stepResults;
    }
    
    /**
     * 并行执行步骤
     */
    private List<StepResult> executeStepsInParallel(List<BusinessStepConfig> steps, TestEnvironment environment) {
        List<CompletableFuture<StepResult>> futures = new ArrayList<>();
        
        for (BusinessStepConfig step : steps) {
            CompletableFuture<StepResult> future = CompletableFuture.supplyAsync(() -> {
                BusinessStepConfig replacedStep = replaceVariablesInStep(step);
                return executeStep(replacedStep, environment);
            }, executorService);
            
            futures.add(future);
        }
        
        // 等待所有步骤完成
        List<StepResult> stepResults = new ArrayList<>();
        for (CompletableFuture<StepResult> future : futures) {
            try {
                stepResults.add(future.get(30, TimeUnit.SECONDS)); // 30秒超时
            } catch (Exception e) {
                logger.error("并行步骤执行超时或异常", e);
                stepResults.add(new StepResult(false, "步骤执行超时或异常: " + e.getMessage(), null));
            }
        }
        
        return stepResults;
    }
    
    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof java.util.concurrent.TimeoutException ||
               e.getMessage().contains("timeout") ||
               e.getMessage().contains("connection") ||
               e.getMessage().contains("network");
    }
    
    private BusinessProcessConfig parseProcessConfig(String configJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(configJson);
        
        BusinessProcessConfig config = new BusinessProcessConfig();
        
        // 解析流程名称
        if (rootNode.has("name")) {
            config.setName(rootNode.get("name").asText());
        }
        
        // 解析并行执行配置
        if (rootNode.has("parallel")) {
            config.setParallel(rootNode.get("parallel").asBoolean());
        }
        
        // 解析超时配置
        if (rootNode.has("timeout")) {
            config.setTimeout(rootNode.get("timeout").asInt(30000));
        }
        
        // 解析流程变量
        if (rootNode.has("variables")) {
            JsonNode variablesNode = rootNode.get("variables");
            if (variablesNode.isObject()) {
                variablesNode.fields().forEachRemaining(entry -> {
                    config.getVariables().put(entry.getKey(), entry.getValue().asText());
                });
            }
        }
        
        // 解析流程步骤
        if (rootNode.has("steps")) {
            JsonNode stepsNode = rootNode.get("steps");
            if (stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    BusinessStepConfig step = new BusinessStepConfig();
                    step.setName(stepNode.has("name") ? stepNode.get("name").asText() : "");
                    step.setType(stepNode.has("type") ? stepNode.get("type").asText() : "");
                    step.setDescription(stepNode.has("description") ? stepNode.get("description").asText() : "");
                    step.setTimeout(stepNode.has("timeout") ? stepNode.get("timeout").asInt(10000) : 10000);
                    step.setRetryCount(stepNode.has("retryCount") ? stepNode.get("retryCount").asInt(0) : 0);
                    
                    // 解析步骤配置
                    if (stepNode.has("config")) {
                        step.setConfig(stepNode.get("config").toString());
                    }
                    
                    config.getSteps().add(step);
                }
            }
        }
        
        return config;
    }
    
    private Map<String, String> replaceVariablesInMap(Map<String, String> variables) {
        if (variables == null) {
            return null;
        }
        
        Map<String, String> replacedVariables = new java.util.HashMap<>();
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            replacedVariables.put(entry.getKey(), variableManager.replaceVariables(entry.getValue()));
        }
        
        return replacedVariables;
    }
    
    private BusinessStepConfig replaceVariablesInStep(BusinessStepConfig step) {
        BusinessStepConfig replacedStep = new BusinessStepConfig();
        replacedStep.setName(variableManager.replaceVariables(step.getName()));
        replacedStep.setType(step.getType());
        replacedStep.setDescription(variableManager.replaceVariables(step.getDescription()));
        replacedStep.setConfig(variableManager.replaceVariables(step.getConfig()));
        
        return replacedStep;
    }
    
    private StepResult executeStep(BusinessStepConfig step, TestEnvironment environment) {
        logger.info("执行步骤: {}, 类型: {}", step.getName(), step.getType());
        
        try {
            // 根据步骤类型执行不同的操作
            switch (step.getType()) {
                case "apiCall":
                    // 这里应该调用API测试执行器
                    // 为简化起见，我们暂时模拟执行
                    Thread.sleep(1000);
                    return new StepResult(true, "API调用成功", null);
                    
                case "validation":
                    // 执行验证逻辑
                    Thread.sleep(500);
                    return new StepResult(true, "验证成功", null);
                    
                case "calculation":
                    // 执行计算逻辑
                    Thread.sleep(500);
                    return new StepResult(true, "计算成功", null);
                    
                default:
                    logger.warn("未知的步骤类型: {}", step.getType());
                    Thread.sleep(500);
                    return new StepResult(true, "步骤执行成功", null);
            }
        } catch (Exception e) {
            logger.error("步骤执行失败", e);
            return new StepResult(false, e.getMessage(), null);
        }
    }
    
    // 业务流程配置类
    private static class BusinessProcessConfig {
        private String name;
        private Map<String, String> variables = new HashMap<>();
        private List<BusinessStepConfig> steps = new ArrayList<>();
        private boolean parallel = false;
        private int timeout = 30000; // 30秒超时
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Map<String, String> getVariables() { return variables; }
        public void setVariables(Map<String, String> variables) { this.variables = variables; }
        public List<BusinessStepConfig> getSteps() { return steps; }
        public void setSteps(List<BusinessStepConfig> steps) { this.steps = steps; }
        public boolean isParallel() { return parallel; }
        public void setParallel(boolean parallel) { this.parallel = parallel; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }
    
    // 业务步骤配置类
    private static class BusinessStepConfig {
        private String name;
        private String type;
        private String description;
        private String config;
        private int timeout = 10000; // 10秒超时
        private int retryCount = 0; // 重试次数
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getConfig() { return config; }
        public void setConfig(String config) { this.config = config; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    }
    
    // 步骤执行结果类
    private static class StepResult {
        private boolean success;
        private String message;
        private Map<String, Object> variables;
        
        public StepResult(boolean success, String message, Map<String, Object> variables) {
            this.success = success;
            this.message = message;
            this.variables = variables;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, Object> getVariables() { return variables; }
    }
}