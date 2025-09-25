package com.testplatform.controller;

import com.testplatform.model.GlobalVariable;
import com.testplatform.model.User;
import com.testplatform.service.GlobalVariableService;
import com.testplatform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局变量控制器
 * 处理全局变量相关的API请求
 */
@RestController
@RequestMapping("/api/variables")
@CrossOrigin(origins = "*")
public class GlobalVariableController {

    @Autowired
    private GlobalVariableService globalVariableService;
    
    @Autowired
    private UserService userService;

    /**
     * 从认证信息中获取用户ID
     */
    private String getUserIdFromAuthentication(Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        return user.getId();
    }

    /**
     * 获取所有变量
     */
    @GetMapping
    public ResponseEntity<?> getAllVariables() {
        try {
            List<GlobalVariable> variables = globalVariableService.getAllVariables();
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据环境ID获取变量
     */
    @GetMapping("/environment/{environmentId}")
    public ResponseEntity<?> getVariablesByEnvironment(@PathVariable String environmentId) {
        try {
            List<GlobalVariable> variables = globalVariableService.getVariablesByEnvironmentId(environmentId);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据ID获取变量
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariableById(@PathVariable String id) {
        try {
            GlobalVariable variable = globalVariableService.findById(id);
            if (variable == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "变量不存在");
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(variable);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据名称搜索变量
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchVariablesByName(@RequestParam String name) {
        try {
            List<GlobalVariable> variables = globalVariableService.searchVariablesByName(name);
            return ResponseEntity.ok(variables);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 创建变量
     */
    @PostMapping
    public ResponseEntity<?> createVariable(@Valid @RequestBody GlobalVariable variable, 
                                            Authentication authentication) {
        try {
            // 从认证信息中获取用户ID，而不是用户名
            String createdBy = getUserIdFromAuthentication(authentication);
            GlobalVariable createdVariable = globalVariableService.createVariable(variable, createdBy);
            return ResponseEntity.ok(createdVariable);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 批量创建变量
     */
    @PostMapping("/batch")
    public ResponseEntity<?> createVariables(@Valid @RequestBody List<GlobalVariable> variables, 
                                             Authentication authentication) {
        try {
            String createdBy = getUserIdFromAuthentication(authentication);
            List<GlobalVariable> createdVariables = globalVariableService.createVariables(variables, createdBy);
            return ResponseEntity.ok(createdVariables);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 更新变量
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVariable(@PathVariable String id, 
                                            @Valid @RequestBody GlobalVariable variable,
                                            Authentication authentication) {
        try {
            String updatedBy = getUserIdFromAuthentication(authentication);
            GlobalVariable updatedVariable = globalVariableService.updateVariable(id, variable, updatedBy);
            return ResponseEntity.ok(updatedVariable);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 删除变量
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteVariable(@PathVariable String id) {
        try {
            globalVariableService.deleteVariable(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "变量删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 替换文本中的变量
     */
    @PostMapping("/replace")
    public ResponseEntity<?> replaceVariables(@RequestBody Map<String, String> request) {
        try {
            String text = request.get("text");
            String environmentId = request.get("environmentId");
            
            if (text == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "文本不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            String result;
            if (environmentId != null && !environmentId.isEmpty()) {
                result = globalVariableService.replaceVariables(text, environmentId);
            } else {
                result = globalVariableService.replaceVariables(text);
            }

            Map<String, String> response = new HashMap<>();
            response.put("originalText", text);
            response.put("replacedText", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 复制变量到其他环境
     */
    @PostMapping("/copy")
    public ResponseEntity<?> copyVariablesToEnvironment(@RequestBody Map<String, String> request,
                                                        Authentication authentication) {
        try {
            String sourceEnvironmentId = request.get("sourceEnvironmentId");
            String targetEnvironmentId = request.get("targetEnvironmentId");
            
            if (sourceEnvironmentId == null || targetEnvironmentId == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "源环境ID和目标环境ID不能为空");
                return ResponseEntity.badRequest().body(error);
            }

            String createdBy = getUserIdFromAuthentication(authentication);
            List<GlobalVariable> copiedVariables = globalVariableService.copyVariablesToEnvironment(
                sourceEnvironmentId, targetEnvironmentId, createdBy);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "变量复制成功");
            response.put("copiedCount", copiedVariables.size());
            response.put("variables", copiedVariables);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 加密变量值
     */
    @PostMapping("/{id}/encrypt")
    public ResponseEntity<?> encryptVariable(@PathVariable String id) {
        try {
            GlobalVariable variable = globalVariableService.findById(id);
            if (variable == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "变量不存在");
                return ResponseEntity.notFound().build();
            }

            if (variable.getIsEncrypted()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "变量已经加密");
                return ResponseEntity.badRequest().body(error);
            }

            String encryptedValue = globalVariableService.encryptValue(variable.getValue());
            variable.setValue(encryptedValue);
            variable.setIsEncrypted(true);
            variable.setUpdatedAt(java.time.LocalDateTime.now());

            GlobalVariable updatedVariable = globalVariableService.updateVariable(id, variable, "system");
            return ResponseEntity.ok(updatedVariable);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 解密变量值
     */
    @PostMapping("/{id}/decrypt")
    public ResponseEntity<?> decryptVariable(@PathVariable String id) {
        try {
            GlobalVariable variable = globalVariableService.findById(id);
            if (variable == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "变量不存在");
                return ResponseEntity.notFound().build();
            }

            if (!variable.getIsEncrypted()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "变量未加密");
                return ResponseEntity.badRequest().body(error);
            }

            String decryptedValue = globalVariableService.decryptValue(variable.getValue());
            
            Map<String, String> response = new HashMap<>();
            response.put("id", variable.getId());
            response.put("name", variable.getName());
            response.put("decryptedValue", decryptedValue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
