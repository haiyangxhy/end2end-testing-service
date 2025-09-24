package com.testplatform.testing.ui;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.TestCase;
import com.testplatform.model.TestEnvironment;
import com.testplatform.testing.TestExecutionResult;
import com.testplatform.testing.TestExecutor;
import com.testplatform.testing.VariableManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UiTestExecutor implements TestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(UiTestExecutor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    private VariableManager variableManager;
    
    @Override
    public TestExecutionResult execute(TestCase testCase, TestEnvironment environment) {
        return executeWithRetry(testCase, environment, 0);
    }
    
    /**
     * 带重试机制的UI测试执行
     */
    public TestExecutionResult executeWithRetry(TestCase testCase, TestEnvironment environment, int retryCount) {
        WebDriver driver = null;
        long startTime = System.currentTimeMillis();
        int maxRetries = 2; // 最大重试次数
        
        try {
            logger.info("开始执行UI测试: {} (重试次数: {})", testCase.getName(), retryCount);
            
            // 解析UI测试配置
            String configJson = testCase.getConfig();
            if (configJson == null || configJson.trim().isEmpty()) {
                return new TestExecutionResult(false, "UI测试配置为空", System.currentTimeMillis() - startTime);
            }
            
            UITestConfig uiTestConfig = parseTestConfig(configJson);
            
            // 初始化WebDriver
            driver = createWebDriver(uiTestConfig);
            
            // 设置隐式等待
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(uiTestConfig.getImplicitWait()));
            
            // 替换配置中的变量
            uiTestConfig.setBaseUrl(variableManager.replaceVariables(uiTestConfig.getBaseUrl()));
            
            // 如果没有设置基础URL，则使用目标系统配置
            if (uiTestConfig.getBaseUrl() == null || uiTestConfig.getBaseUrl().isEmpty()) {
                if (environment != null && environment.getUiBaseUrl() != null) {
                    uiTestConfig.setBaseUrl(environment.getUiBaseUrl());
                }
            }
            
            // 执行测试步骤
            List<UIStepResult> stepResults = new ArrayList<>();
            boolean testSuccess = true;
            String failureMessage = "";
            
            for (UIStepConfig step : uiTestConfig.getSteps()) {
                // 替换步骤配置中的变量
                step = replaceVariablesInStep(step);
                
                UIStepResult stepResult = executeStepWithRetry(driver, step, 0, environment);
                stepResults.add(stepResult);
                
                if (!stepResult.isSuccess()) {
                    testSuccess = false;
                    failureMessage = "步骤 " + step.getName() + " 执行失败: " + stepResult.getMessage();
                    logger.error(failureMessage);
                    break;
                }
                
                // 更新变量
                if (stepResult.getVariables() != null) {
                    stepResult.getVariables().forEach((key, value) -> {
                        variableManager.setVariable(key, value);
                    });
                }
            }
            
            if (testSuccess) {
                logger.info("UI测试执行成功: {}", testCase.getName());
                return new TestExecutionResult(true, "UI测试执行成功", System.currentTimeMillis() - startTime);
            } else {
                // 如果失败且还有重试次数，则重试
                if (retryCount < maxRetries) {
                    logger.warn("UI测试执行失败，准备重试 (第{}次): {}", retryCount + 1, failureMessage);
                    Thread.sleep(3000 * (retryCount + 1)); // 递增延迟
                    return executeWithRetry(testCase, environment, retryCount + 1);
                }
                
                return new TestExecutionResult(false, failureMessage, System.currentTimeMillis() - startTime);
            }
        } catch (Exception e) {
            if (retryCount < maxRetries && isRetryableException(e)) {
                logger.warn("UI测试执行异常，准备重试 (第{}次): {}", retryCount + 1, e.getMessage());
                try {
                    Thread.sleep(3000 * (retryCount + 1)); // 递增延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return executeWithRetry(testCase, environment, retryCount + 1);
            }
            
            logger.error("UI测试执行失败", e);
            return new TestExecutionResult(false, "UI测试执行异常: " + e.getMessage(), System.currentTimeMillis() - startTime);
        } finally {
            // 关闭浏览器
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception e) {
                    logger.warn("关闭浏览器时出错", e);
                }
            }
        }
    }
    
    /**
     * 创建WebDriver实例
     */
    private WebDriver createWebDriver(UITestConfig config) {
        String browser = config.getBrowser().toLowerCase();
        
        switch (browser) {
            case "firefox":
                return createFirefoxDriver(config);
            case "chrome":
            default:
                return createChromeDriver(config);
        }
    }
    
    /**
     * 创建Chrome WebDriver
     */
    private WebDriver createChromeDriver(UITestConfig config) {
        ChromeOptions options = new ChromeOptions();
        
        // 添加无头模式支持
        if (config.isHeadless()) {
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        
        // 添加其他Chrome选项
        for (String arg : config.getChromeArguments()) {
            options.addArguments(arg);
        }
        
        // 设置窗口大小
        if (config.getWindowSize() != null && !config.getWindowSize().isEmpty()) {
            options.addArguments("--window-size=" + config.getWindowSize());
        }
        
        return new ChromeDriver(options);
    }
    
    /**
     * 创建Firefox WebDriver
     */
    private WebDriver createFirefoxDriver(UITestConfig config) {
        FirefoxOptions options = new FirefoxOptions();
        
        // 添加无头模式支持
        if (config.isHeadless()) {
            options.addArguments("--headless");
        }
        
        // 设置窗口大小
        if (config.getWindowSize() != null && !config.getWindowSize().isEmpty()) {
            String[] size = config.getWindowSize().split("x");
            if (size.length == 2) {
                options.addArguments("--width=" + size[0]);
                options.addArguments("--height=" + size[1]);
            }
        }
        
        return new FirefoxDriver(options);
    }
    
    /**
     * 带重试机制的步骤执行
     */
    private UIStepResult executeStepWithRetry(WebDriver driver, UIStepConfig step, int retryCount, TestEnvironment environment) {
        int maxRetries = step.getRetryCount();
        
        try {
            return executeStep(driver, step, environment);
        } catch (Exception e) {
            if (retryCount < maxRetries && isRetryableException(e)) {
                logger.warn("步骤执行失败，准备重试 (第{}次): {}", retryCount + 1, e.getMessage());
                try {
                    Thread.sleep(1000 * (retryCount + 1)); // 递增延迟
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                return executeStepWithRetry(driver, step, retryCount + 1, environment);
            }
            throw e;
        }
    }
    
    /**
     * 判断异常是否可重试
     */
    private boolean isRetryableException(Exception e) {
        return e instanceof TimeoutException ||
               e instanceof NoSuchElementException ||
               e instanceof StaleElementReferenceException ||
               e.getMessage().contains("timeout") ||
               e.getMessage().contains("element not found");
    }
    
    private UITestConfig parseTestConfig(String configJson) throws IOException {
        JsonNode rootNode = objectMapper.readTree(configJson);
        
        UITestConfig config = new UITestConfig();
        
        // 解析基础URL
        if (rootNode.has("baseUrl")) {
            config.setBaseUrl(rootNode.get("baseUrl").asText());
        }
        
        // 解析浏览器类型
        if (rootNode.has("browser")) {
            config.setBrowser(rootNode.get("browser").asText());
        }
        
        // 解析无头模式
        if (rootNode.has("headless")) {
            config.setHeadless(rootNode.get("headless").asBoolean());
        }
        
        // 解析窗口大小
        if (rootNode.has("windowSize")) {
            config.setWindowSize(rootNode.get("windowSize").asText());
        }
        
        // 解析隐式等待时间
        if (rootNode.has("implicitWait")) {
            config.setImplicitWait(rootNode.get("implicitWait").asInt(10));
        }
        
        // 解析Chrome参数
        if (rootNode.has("chromeArguments")) {
            JsonNode argsNode = rootNode.get("chromeArguments");
            if (argsNode.isArray()) {
                for (JsonNode argNode : argsNode) {
                    config.getChromeArguments().add(argNode.asText());
                }
            }
        }
        
        // 解析测试步骤
        if (rootNode.has("steps")) {
            JsonNode stepsNode = rootNode.get("steps");
            if (stepsNode.isArray()) {
                for (JsonNode stepNode : stepsNode) {
                    UIStepConfig step = new UIStepConfig();
                    step.setName(stepNode.has("name") ? stepNode.get("name").asText() : "");
                    step.setType(stepNode.has("type") ? stepNode.get("type").asText() : "");
                    
                    // 解析定位器
                    if (stepNode.has("locator")) {
                        JsonNode locatorNode = stepNode.get("locator");
                        if (locatorNode.isObject()) {
                            step.setLocatorType(locatorNode.has("type") ? locatorNode.get("type").asText() : "css");
                            step.setLocatorValue(locatorNode.has("value") ? locatorNode.get("value").asText() : "");
                        }
                    }
                    
                    // 解析值
                    if (stepNode.has("value")) {
                        step.setValue(stepNode.get("value").asText());
                    }
                    
                    // 解析超时时间
                    if (stepNode.has("timeout")) {
                        step.setTimeout(stepNode.get("timeout").asInt(10));
                    }
                    
                    // 解析重试次数
                    if (stepNode.has("retryCount")) {
                        step.setRetryCount(stepNode.get("retryCount").asInt(0));
                    }
                    
                    // 解析变量提取配置
                    if (stepNode.has("extractVariable")) {
                        JsonNode extractNode = stepNode.get("extractVariable");
                        if (extractNode.isObject()) {
                            step.setExtractVariableName(extractNode.has("name") ? extractNode.get("name").asText() : "");
                            step.setExtractVariableProperty(extractNode.has("property") ? extractNode.get("property").asText() : "text");
                        }
                    }
                    
                    config.getSteps().add(step);
                }
            }
        }
        
        return config;
    }
    
    private UIStepConfig replaceVariablesInStep(UIStepConfig step) {
        UIStepConfig replacedStep = new UIStepConfig();
        replacedStep.setName(variableManager.replaceVariables(step.getName()));
        replacedStep.setType(step.getType());
        replacedStep.setLocatorType(step.getLocatorType());
        replacedStep.setLocatorValue(variableManager.replaceVariables(step.getLocatorValue()));
        replacedStep.setValue(variableManager.replaceVariables(step.getValue()));
        replacedStep.setTimeout(step.getTimeout());
        replacedStep.setExtractVariableName(step.getExtractVariableName());
        replacedStep.setExtractVariableProperty(step.getExtractVariableProperty());
        
        return replacedStep;
    }
    
    private UIStepResult executeStep(WebDriver driver, UIStepConfig step, TestEnvironment environment) {
        logger.info("执行UI步骤: {}, 类型: {}", step.getName(), step.getType());
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(step.getTimeout()));
            
            // 根据步骤类型执行不同的操作
            switch (step.getType().toLowerCase()) {
                case "navigate":
                    // 导航到URL
                    String url = step.getValue();
                    if (!url.startsWith("http")) {
                        // 如果不是完整URL，添加基础URL
                        UITestConfig config = (UITestConfig) step.getParent();
                        if (environment != null && environment.getUiBaseUrl() != null) {
                            url = environment.getUiBaseUrl() + (url.startsWith("/") ? url : "/" + url);
                        }
                    }
                    driver.get(url);
                    return new UIStepResult(true, "导航到: " + url, null);
                    
                case "click":
                    // 点击元素
                    WebElement clickElement = wait.until(ExpectedConditions.elementToBeClickable(findElement(driver, step)));
                    clickElement.click();
                    return new UIStepResult(true, "点击元素: " + step.getLocatorValue(), null);
                    
                case "type":
                    // 输入文本
                    WebElement inputElement = wait.until(ExpectedConditions.visibilityOfElementLocated(findElement(driver, step)));
                    inputElement.clear();
                    inputElement.sendKeys(step.getValue());
                    return new UIStepResult(true, "在元素输入文本: " + step.getLocatorValue(), null);
                    
                case "wait":
                    // 等待指定时间（秒）
                    Thread.sleep(Integer.parseInt(step.getValue()) * 1000);
                    return new UIStepResult(true, "等待: " + step.getValue() + "秒", null);
                    
                case "assertText":
                    // 断言文本
                    WebElement assertElement = wait.until(ExpectedConditions.visibilityOfElementLocated(findElement(driver, step)));
                    String actualText = assertElement.getText();
                    String expectedText = step.getValue();
                    if (actualText.contains(expectedText)) {
                        return new UIStepResult(true, "文本断言成功", null);
                    } else {
                        return new UIStepResult(false, "文本断言失败: 期望包含'" + expectedText + "', 实际为'" + actualText + "'", null);
                    }
                    
                case "getText":
                    // 获取文本并提取为变量
                    WebElement textElement = wait.until(ExpectedConditions.visibilityOfElementLocated(findElement(driver, step)));
                    String text = textElement.getText();
                    Map<String, Object> variables = new HashMap<>();
                    variables.put(step.getExtractVariableName(), text);
                    return new UIStepResult(true, "获取文本并设置变量: " + step.getExtractVariableName(), variables);
                    
                default:
                    logger.warn("未知的UI步骤类型: {}", step.getType());
                    return new UIStepResult(false, "未知的步骤类型", null);
            }
        } catch (NoSuchElementException e) {
            logger.error("找不到元素: {}", step.getLocatorValue(), e);
            return new UIStepResult(false, "找不到元素: " + step.getLocatorValue(), null);
        } catch (Exception e) {
            logger.error("步骤执行失败", e);
            return new UIStepResult(false, e.getMessage(), null);
        }
    }
    
    private By findElement(WebDriver driver, UIStepConfig step) {
        String locatorType = step.getLocatorType().toLowerCase();
        String locatorValue = step.getLocatorValue();
        
        switch (locatorType) {
            case "id":
                return By.id(locatorValue);
            case "name":
                return By.name(locatorValue);
            case "class":
            case "classname":
                return By.className(locatorValue);
            case "xpath":
                return By.xpath(locatorValue);
            case "linktext":
                return By.linkText(locatorValue);
            case "partiallinktext":
                return By.partialLinkText(locatorValue);
            case "css":
            case "cssselector":
            default:
                return By.cssSelector(locatorValue);
        }
    }
    
    // UI测试配置类
    private static class UITestConfig {
        private String baseUrl;
        private String browser = "chrome";
        private boolean headless = true;
        private String windowSize = "1920x1080";
        private int implicitWait = 10;
        private List<String> chromeArguments = new ArrayList<>();
        private List<UIStepConfig> steps = new ArrayList<>();
        
        // Getters and Setters
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getBrowser() { return browser; }
        public void setBrowser(String browser) { this.browser = browser; }
        public boolean isHeadless() { return headless; }
        public void setHeadless(boolean headless) { this.headless = headless; }
        public String getWindowSize() { return windowSize; }
        public void setWindowSize(String windowSize) { this.windowSize = windowSize; }
        public int getImplicitWait() { return implicitWait; }
        public void setImplicitWait(int implicitWait) { this.implicitWait = implicitWait; }
        public List<String> getChromeArguments() { return chromeArguments; }
        public void setChromeArguments(List<String> chromeArguments) { this.chromeArguments = chromeArguments; }
        public List<UIStepConfig> getSteps() { return steps; }
        public void setSteps(List<UIStepConfig> steps) { this.steps = steps; }
    }
    
    // UI步骤配置类
    private static class UIStepConfig {
        private String name;
        private String type;
        private String locatorType = "css";
        private String locatorValue;
        private String value;
        private int timeout = 10;
        private int retryCount = 0;
        private String extractVariableName;
        private String extractVariableProperty = "text";
        private Object parent;
        
        // Getters and Setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getLocatorType() { return locatorType; }
        public void setLocatorType(String locatorType) { this.locatorType = locatorType; }
        public String getLocatorValue() { return locatorValue; }
        public void setLocatorValue(String locatorValue) { this.locatorValue = locatorValue; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
        public int getRetryCount() { return retryCount; }
        public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
        public String getExtractVariableName() { return extractVariableName; }
        public void setExtractVariableName(String extractVariableName) { this.extractVariableName = extractVariableName; }
        public String getExtractVariableProperty() { return extractVariableProperty; }
        public void setExtractVariableProperty(String extractVariableProperty) { this.extractVariableProperty = extractVariableProperty; }
        public Object getParent() { return parent; }
        public void setParent(Object parent) { this.parent = parent; }
    }
    
    // UI步骤执行结果类
    private static class UIStepResult {
        private boolean success;
        private String message;
        private Map<String, Object> variables;
        
        public UIStepResult(boolean success, String message, Map<String, Object> variables) {
            this.success = success;
            this.message = message;
            this.variables = variables;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Map<String, Object> getVariables() { return variables; }
    }
}