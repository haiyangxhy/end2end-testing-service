package com.testplatform.controller;

import com.testplatform.model.User;
import com.testplatform.model.dto.LoginRequest;
import com.testplatform.model.dto.LoginResponse;
import com.testplatform.model.dto.RegisterRequest;
import com.testplatform.service.UserService;
import com.testplatform.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 处理用户登录、注册、登出等认证相关功能
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.login(loginRequest);
            
            String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole());
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
            Long expiresIn = jwtUtil.getRemainingTime(token);

            LoginResponse response = new LoginResponse(
                token, 
                refreshToken, 
                expiresIn, 
                user.getUsername(), 
                user.getRole(), 
                user.getFullName()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.register(registerRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "注册成功");
            response.put("userId", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("无效的认证头");
            }

            String token = authHeader.substring(7);
            
            if (!jwtUtil.isValidToken(token)) {
                throw new RuntimeException("无效的Token");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            String userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            User user = userService.findByUsername(username);
            if (user == null || !user.getIsActive()) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            String newToken = jwtUtil.generateToken(username, userId, role);
            String newRefreshToken = jwtUtil.generateRefreshToken(username, userId);
            Long expiresIn = jwtUtil.getRemainingTime(newToken);

            LoginResponse response = new LoginResponse(
                newToken, 
                newRefreshToken, 
                expiresIn, 
                username, 
                role, 
                user.getFullName()
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * 验证Token
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("无效的认证头");
            }

            String token = authHeader.substring(7);
            
            if (!jwtUtil.isValidToken(token)) {
                throw new RuntimeException("无效的Token");
            }

            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null || !user.getIsActive()) {
                throw new RuntimeException("用户不存在或已被禁用");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("username", username);
            response.put("userId", user.getId());
            response.put("role", user.getRole());
            response.put("expiresIn", jwtUtil.getRemainingTime(token));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valid", false);
            response.put("error", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        // 在实际应用中，这里可以将Token加入黑名单
        // 目前只是返回成功响应
        Map<String, String> response = new HashMap<>();
        response.put("message", "登出成功");
        return ResponseEntity.ok(response);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new RuntimeException("无效的认证头");
            }

            String token = authHeader.substring(7);
            String username = jwtUtil.getUsernameFromToken(token);
            User user = userService.findByUsername(username);
            
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullName());
            response.put("role", user.getRole());
            response.put("isActive", user.getIsActive());
            response.put("lastLogin", user.getLastLogin());
            response.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
