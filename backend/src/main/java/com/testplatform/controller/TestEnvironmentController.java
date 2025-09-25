package com.testplatform.controller;

import com.testplatform.model.TestEnvironment;
import com.testplatform.model.User;
import com.testplatform.service.TestEnvironmentService;
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
 * 测试环境控制器
 * 处理测试环境相关的API请求
 */
@RestController
@RequestMapping("/api/environments")
@CrossOrigin(origins = "*")
public class TestEnvironmentController {

    @Autowired
    private TestEnvironmentService testEnvironmentService;
    
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
     * 获取所有环境
     */
    @GetMapping
    public ResponseEntity<?> getAllEnvironments() {
        try {
            List<TestEnvironment> environments = testEnvironmentService.getAllEnvironments();
            return ResponseEntity.ok(environments);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 根据ID获取环境
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEnvironmentById(@PathVariable String id) {
        try {
            TestEnvironment environment = testEnvironmentService.findById(id);
            if (environment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "环境不存在");
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(environment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 获取活跃环境
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveEnvironment() {
        try {
            TestEnvironment environment = testEnvironmentService.getActiveEnvironment();
            if (environment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "没有活跃环境");
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(environment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 创建环境
     */
    @PostMapping
    public ResponseEntity<?> createEnvironment(@Valid @RequestBody TestEnvironment environment, 
                                               Authentication authentication) {
        try {
            // 获取用户ID而不是用户名
            String createdBy = getUserIdFromAuthentication(authentication);
            TestEnvironment createdEnvironment = testEnvironmentService.createEnvironment(environment, createdBy);
            return ResponseEntity.ok(createdEnvironment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 更新环境
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEnvironment(@PathVariable String id, 
                                               @Valid @RequestBody TestEnvironment environment,
                                               Authentication authentication) {
        try {
            // 获取用户ID而不是用户名
            String updatedBy = getUserIdFromAuthentication(authentication);
            TestEnvironment updatedEnvironment = testEnvironmentService.updateEnvironment(id, environment, updatedBy);
            return ResponseEntity.ok(updatedEnvironment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 删除环境
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEnvironment(@PathVariable String id) {
        try {
            testEnvironmentService.deleteEnvironment(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "环境删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 激活环境
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activateEnvironment(@PathVariable String id) {
        try {
            TestEnvironment environment = testEnvironmentService.activateEnvironment(id);
            return ResponseEntity.ok(environment);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 验证环境配置
     */
    @PostMapping("/{id}/validate")
    public ResponseEntity<?> validateEnvironment(@PathVariable String id) {
        try {
            TestEnvironment environment = testEnvironmentService.findById(id);
            if (environment == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "环境不存在");
                return ResponseEntity.notFound().build();
            }

            boolean isValid = testEnvironmentService.validateEnvironmentConfig(environment);
            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("message", isValid ? "环境配置有效" : "环境配置无效");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 测试环境连接
     */
    @PostMapping("/{id}/test-connection")
    public ResponseEntity<?> testEnvironmentConnection(@PathVariable String id) {
        try {
            boolean isConnected = testEnvironmentService.testEnvironmentConnection(id);
            Map<String, Object> response = new HashMap<>();
            response.put("connected", isConnected);
            response.put("message", isConnected ? "连接成功" : "连接失败");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 全面测试环境连接（包括API、UI、数据库和认证）
     */
    @PostMapping("/{id}/test-full-connection")
    public ResponseEntity<?> testFullEnvironmentConnection(@PathVariable String id) {
        try {
            com.testplatform.model.TestConnectionResult result = testEnvironmentService.testFullEnvironmentConnection(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
