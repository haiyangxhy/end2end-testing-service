package com.testplatform.service;

import com.testplatform.model.GlobalVariable;
import com.testplatform.repository.GlobalVariableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * 全局变量服务类
 * 处理全局变量的创建、更新、删除和查询
 */
@Service
@Transactional
public class GlobalVariableService {

    @Autowired
    private GlobalVariableRepository globalVariableRepository;

    /**
     * 创建全局变量
     */
    public GlobalVariable createVariable(GlobalVariable variable, String createdBy) {
        // 验证变量名称唯一性（在同一环境下）
        if (globalVariableRepository.findByNameAndEnvironmentId(variable.getName(), variable.getEnvironmentId()).isPresent()) {
            throw new RuntimeException("变量名称已存在: " + variable.getName());
        }

        // 验证变量类型
        if (!isValidVariableType(variable.getVariableType())) {
            throw new RuntimeException("无效的变量类型: " + variable.getVariableType());
        }

        // 验证变量值格式
        if (!isValidVariableValue(variable.getValue(), variable.getVariableType())) {
            throw new RuntimeException("变量值格式不正确");
        }

        // 设置默认值
        variable.setId(java.util.UUID.randomUUID().toString());
        variable.setCreatedBy(createdBy);
        variable.setCreatedAt(LocalDateTime.now());
        variable.setUpdatedAt(LocalDateTime.now());

        return globalVariableRepository.save(variable);
    }

    /**
     * 更新全局变量
     */
    public GlobalVariable updateVariable(String id, GlobalVariable variable, String updatedBy) {
        GlobalVariable existingVariable = findById(id);
        if (existingVariable == null) {
            throw new RuntimeException("变量不存在: " + id);
        }

        // 验证变量名称唯一性（排除自己）
        Optional<GlobalVariable> nameConflict = globalVariableRepository.findByNameAndEnvironmentId(
            variable.getName(), variable.getEnvironmentId());
        if (nameConflict.isPresent() && !nameConflict.get().getId().equals(id)) {
            throw new RuntimeException("变量名称已存在: " + variable.getName());
        }

        // 验证变量类型
        if (!isValidVariableType(variable.getVariableType())) {
            throw new RuntimeException("无效的变量类型: " + variable.getVariableType());
        }

        // 验证变量值格式
        if (!isValidVariableValue(variable.getValue(), variable.getVariableType())) {
            throw new RuntimeException("变量值格式不正确");
        }

        // 更新字段
        existingVariable.setName(variable.getName());
        existingVariable.setValue(variable.getValue());
        existingVariable.setDescription(variable.getDescription());
        existingVariable.setEnvironmentId(variable.getEnvironmentId());
        existingVariable.setVariableType(variable.getVariableType());
        existingVariable.setIsEncrypted(variable.getIsEncrypted());
        existingVariable.setUpdatedAt(LocalDateTime.now());

        return globalVariableRepository.save(existingVariable);
    }

    /**
     * 删除全局变量
     */
    public void deleteVariable(String id) {
        GlobalVariable variable = findById(id);
        if (variable == null) {
            throw new RuntimeException("变量不存在: " + id);
        }

        globalVariableRepository.delete(variable);
    }

    /**
     * 根据ID查找变量
     */
    public GlobalVariable findById(String id) {
        return globalVariableRepository.findById(id).orElse(null);
    }

    /**
     * 根据名称和环境ID查找变量
     */
    public GlobalVariable findByNameAndEnvironmentId(String name, String environmentId) {
        return globalVariableRepository.findByNameAndEnvironmentId(name, environmentId).orElse(null);
    }

    /**
     * 获取指定环境的所有变量
     */
    public List<GlobalVariable> getVariablesByEnvironmentId(String environmentId) {
        return globalVariableRepository.findByEnvironmentId(environmentId);
    }

    /**
     * 获取所有变量
     */
    public List<GlobalVariable> getAllVariables() {
        return globalVariableRepository.findAll();
    }

    /**
     * 根据名称搜索变量
     */
    public List<GlobalVariable> searchVariablesByName(String name) {
        return globalVariableRepository.findByNameContainingIgnoreCase(name);
    }

    /**
     * 验证变量类型
     */
    private boolean isValidVariableType(String type) {
        return type != null && (type.equals("STRING") || type.equals("NUMBER") || 
                               type.equals("BOOLEAN") || type.equals("JSON"));
    }

    /**
     * 验证变量值格式
     */
    private boolean isValidVariableValue(String value, String type) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        switch (type) {
            case "STRING":
                return true; // 字符串类型不需要特殊验证
            case "NUMBER":
                try {
                    Double.parseDouble(value);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            case "BOOLEAN":
                return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false");
            case "JSON":
                try {
                    // 简单的JSON格式验证
                    return value.trim().startsWith("{") && value.trim().endsWith("}") ||
                           value.trim().startsWith("[") && value.trim().endsWith("]");
                } catch (Exception e) {
                    return false;
                }
            default:
                return false;
        }
    }

    /**
     * 替换字符串中的变量
     */
    public String replaceVariables(String text, String environmentId) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        List<GlobalVariable> variables = getVariablesByEnvironmentId(environmentId);
        String result = text;

        for (GlobalVariable variable : variables) {
            String placeholder = "${" + variable.getName() + "}";
            String value = variable.getIsEncrypted() ? 
                decryptValue(variable.getValue()) : variable.getValue();
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * 替换字符串中的变量（使用活跃环境）
     */
    public String replaceVariables(String text) {
        // 这里需要获取活跃环境的ID
        // 暂时使用默认环境ID
        return replaceVariables(text, "env-001");
    }

    /**
     * 加密变量值
     */
    public String encryptValue(String value) {
        // 简单的Base64编码，实际项目中应使用更安全的加密方法
        return java.util.Base64.getEncoder().encodeToString(value.getBytes());
    }

    /**
     * 解密变量值
     */
    public String decryptValue(String encryptedValue) {
        try {
            return new String(java.util.Base64.getDecoder().decode(encryptedValue));
        } catch (Exception e) {
            return encryptedValue; // 如果解密失败，返回原值
        }
    }

    /**
     * 批量创建变量
     */
    public List<GlobalVariable> createVariables(List<GlobalVariable> variables, String createdBy) {
        for (GlobalVariable variable : variables) {
            createVariable(variable, createdBy);
        }
        return variables;
    }

    /**
     * 复制变量到其他环境
     */
    public List<GlobalVariable> copyVariablesToEnvironment(String sourceEnvironmentId, 
                                                           String targetEnvironmentId, 
                                                           String createdBy) {
        List<GlobalVariable> sourceVariables = getVariablesByEnvironmentId(sourceEnvironmentId);
        List<GlobalVariable> copiedVariables = new java.util.ArrayList<>();

        for (GlobalVariable sourceVariable : sourceVariables) {
            GlobalVariable newVariable = new GlobalVariable();
            newVariable.setName(sourceVariable.getName());
            newVariable.setValue(sourceVariable.getValue());
            newVariable.setDescription(sourceVariable.getDescription());
            newVariable.setEnvironmentId(targetEnvironmentId);
            newVariable.setVariableType(sourceVariable.getVariableType());
            newVariable.setIsEncrypted(sourceVariable.getIsEncrypted());
            
            copiedVariables.add(createVariable(newVariable, createdBy));
        }

        return copiedVariables;
    }
}
