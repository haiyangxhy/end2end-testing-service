package com.testplatform;

import com.testplatform.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

/**
 * JWT测试类
 * 用于验证JWT密钥和Token生成
 */
@SpringBootTest
public class JwtTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    public void testJwtTokenGeneration() {
        // 测试生成JWT Token
        String username = "admin";
        String userId = "admin-001";
        String role = "ADMIN";
        
        String token = jwtUtil.generateToken(username, userId, role);
        assertNotNull(token, "Token不应该为null");
        assertFalse(token.isEmpty(), "Token不应该为空");
        
        System.out.println("生成的JWT Token: " + token);
        
        // 验证Token
        assertTrue(jwtUtil.isValidToken(token), "Token应该有效");
        assertEquals(username, jwtUtil.getUsernameFromToken(token), "用户名应该匹配");
        assertEquals(userId, jwtUtil.getUserIdFromToken(token), "用户ID应该匹配");
        assertEquals(role, jwtUtil.getRoleFromToken(token), "角色应该匹配");
        
        System.out.println("JWT Token验证成功！");
    }
    
    @Test
    public void testRefreshTokenGeneration() {
        // 测试生成刷新Token
        String username = "admin";
        String userId = "admin-001";
        
        String refreshToken = jwtUtil.generateRefreshToken(username, userId);
        assertNotNull(refreshToken, "刷新Token不应该为null");
        assertFalse(refreshToken.isEmpty(), "刷新Token不应该为空");
        
        System.out.println("生成的刷新Token: " + refreshToken);
        
        // 验证刷新Token
        assertTrue(jwtUtil.isValidToken(refreshToken), "刷新Token应该有效");
        assertEquals(username, jwtUtil.getUsernameFromToken(refreshToken), "用户名应该匹配");
        assertEquals(userId, jwtUtil.getUserIdFromToken(refreshToken), "用户ID应该匹配");
        
        System.out.println("刷新Token验证成功！");
    }
}
