package com.testplatform.testing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testplatform.model.GlobalVariable;
import com.testplatform.service.GlobalVariableService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 变量管理器，用于管理测试过程中的变量替换和提取
 * 支持全局变量、局部变量、系统变量和动态变量
 */
@Component
public class VariableManager {
    private static final Logger logger = LoggerFactory.getLogger(VariableManager.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(\\w+(?:\\.\\w+)*)}");
    private static final Pattern SYSTEM_VARIABLE_PATTERN = Pattern.compile("\\$\\{__([^}]+)}");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Object> localVariables = new HashMap<>();
    private final Map<String, Object> sessionVariables = new HashMap<>();
    
    @Autowired
    private GlobalVariableService globalVariableService;
    
    private String currentEnvironmentId = "env-001"; // 默认环境ID
    
    /**
     * 设置当前环境ID
     * @param environmentId 环境ID
     */
    public void setCurrentEnvironmentId(String environmentId) {
        this.currentEnvironmentId = environmentId;
        logger.info("设置当前环境ID: {}", environmentId);
    }
    
    /**
     * 设置局部变量
     * @param name 变量名
     * @param value 变量值
     */
    public void setLocalVariable(String name, Object value) {
        logger.info("设置局部变量: {} = {}", name, value);
        localVariables.put(name, value);
    }
    
    /**
     * 设置会话变量
     * @param name 变量名
     * @param value 变量值
     */
    public void setSessionVariable(String name, Object value) {
        logger.info("设置会话变量: {} = {}", name, value);
        sessionVariables.put(name, value);
    }
    
    /**
     * 设置变量（兼容旧接口）
     * @param name 变量名
     * @param value 变量值
     */
    public void setVariable(String name, Object value) {
        setLocalVariable(name, value);
    }
    
    /**
     * 获取变量（按优先级查找）
     * @param name 变量名
     * @return 变量值
     */
    public Object getVariable(String name) {
        // 1. 首先查找局部变量
        if (localVariables.containsKey(name)) {
            return localVariables.get(name);
        }
        
        // 2. 查找会话变量
        if (sessionVariables.containsKey(name)) {
            return sessionVariables.get(name);
        }
        
        // 3. 查找全局变量
        GlobalVariable globalVariable = globalVariableService.findByNameAndEnvironmentId(name, currentEnvironmentId);
        if (globalVariable != null) {
            return globalVariable.getValue();
        }
        
        // 4. 查找系统变量
        return getSystemVariable(name);
    }
    
    /**
     * 获取系统变量
     * @param name 变量名
     * @return 变量值
     */
    private Object getSystemVariable(String name) {
        switch (name.toLowerCase()) {
            case "timestamp":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "date":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "time":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "datetime":
                return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "unix_timestamp":
                return System.currentTimeMillis() / 1000;
            case "random_int":
                return (int) (Math.random() * 10000);
            case "random_string":
                return "random_" + System.currentTimeMillis();
            case "uuid":
                return java.util.UUID.randomUUID().toString();
            default:
                return null;
        }
    }
    
    /**
     * 替换字符串中的变量（支持多种变量类型）
     * @param input 输入字符串
     * @return 替换后的字符串
     */
    public String replaceVariables(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        String result = input;
        
        // 替换普通变量 ${variable}
        result = replacePatternVariables(result, VARIABLE_PATTERN);
        
        // 替换系统变量 ${__system_variable}
        result = replacePatternVariables(result, SYSTEM_VARIABLE_PATTERN);
        
        return result;
    }
    
    /**
     * 替换指定模式的变量
     * @param input 输入字符串
     * @param pattern 变量模式
     * @return 替换后的字符串
     */
    private String replacePatternVariables(String input, Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = getVariable(variableName);
            
            if (value != null) {
                matcher.appendReplacement(sb, String.valueOf(value));
                logger.debug("替换变量: {} = {}", variableName, value);
            } else {
                logger.warn("变量未找到: {}", variableName);
                // 保持原样
                matcher.appendReplacement(sb, matcher.group());
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    /**
     * 从JSON响应中提取变量
     * @param responseBody 响应体
     * @param extractors 提取器配置
     */
    public void extractVariablesFromJson(String responseBody, JsonNode extractors) {
        if (responseBody == null || responseBody.isEmpty() || extractors == null || !extractors.isArray()) {
            return;
        }
        
        try {
            JsonNode responseJson = objectMapper.readTree(responseBody);
            
            for (JsonNode extractor : extractors) {
                String type = extractor.get("type").asText();
                String expression = extractor.get("expression").asText();
                String variableName = extractor.get("variable").asText();
                
                if ("json".equals(type)) {
                    Object value = extractValueFromJson(responseJson, expression);
                    if (value != null) {
                        setVariable(variableName, value);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("解析JSON响应失败", e);
        }
    }
    
    /**
     * 从JSON中提取值
     * @param json JSON节点
     * @param expression JSON路径表达式
     * @return 提取的值
     */
    private Object extractValueFromJson(JsonNode json, String expression) {
        // 简单的JSON路径解析，支持.操作符
        if (expression.startsWith("$")) {
            expression = expression.substring(1);
        }
        
        String[] parts = expression.split("\\.");
        JsonNode current = json;
        
        for (String part : parts) {
            if (current == null || !current.isObject() || !current.has(part)) {
                logger.warn("无法在JSON中找到路径: {}", expression);
                return null;
            }
            current = current.get(part);
        }
        
        return getValueFromNode(current);
    }
    
    /**
     * 从JsonNode获取值
     * @param node JsonNode
     * @return Java对象值
     */
    private Object getValueFromNode(JsonNode node) {
        if (node.isInt()) return node.asInt();
        if (node.isBoolean()) return node.asBoolean();
        if (node.isDouble()) return node.asDouble();
        if (node.isLong()) return node.asLong();
        if (node.isTextual()) return node.asText();
        if (node.isNull()) return null;
        
        // 对于复杂对象，返回字符串表示
        return node.toString();
    }
    
    /**
     * 清除局部变量
     */
    public void clearLocalVariables() {
        localVariables.clear();
        logger.info("已清除局部变量");
    }
    
    /**
     * 清除会话变量
     */
    public void clearSessionVariables() {
        sessionVariables.clear();
        logger.info("已清除会话变量");
    }
    
    /**
     * 清除所有变量
     */
    public void clear() {
        clearLocalVariables();
        clearSessionVariables();
        logger.info("已清除所有变量");
    }
    
    /**
     * 获取所有局部变量
     * @return 变量映射
     */
    public Map<String, Object> getAllLocalVariables() {
        return new HashMap<>(localVariables);
    }
    
    /**
     * 获取所有会话变量
     * @return 变量映射
     */
    public Map<String, Object> getAllSessionVariables() {
        return new HashMap<>(sessionVariables);
    }
    
    /**
     * 获取所有变量（兼容旧接口）
     * @return 变量映射
     */
    public Map<String, Object> getAllVariables() {
        Map<String, Object> allVariables = new HashMap<>();
        allVariables.putAll(localVariables);
        allVariables.putAll(sessionVariables);
        return allVariables;
    }
    
    /**
     * 批量设置变量
     * @param variables 变量映射
     */
    public void setVariables(Map<String, Object> variables) {
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            setLocalVariable(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * 检查变量是否存在
     * @param name 变量名
     * @return 是否存在
     */
    public boolean hasVariable(String name) {
        return getVariable(name) != null;
    }
    
    /**
     * 删除变量
     * @param name 变量名
     * @return 是否删除成功
     */
    public boolean removeVariable(String name) {
        boolean removed = false;
        if (localVariables.containsKey(name)) {
            localVariables.remove(name);
            removed = true;
        }
        if (sessionVariables.containsKey(name)) {
            sessionVariables.remove(name);
            removed = true;
        }
        return removed;
    }
}