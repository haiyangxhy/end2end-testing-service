package com.testplatform;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 密码测试类
 * 用于验证数据库中的密码哈希是否正确
 */
public class PasswordTest {

    @Test
    public void testPasswordHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // 测试密码 admin123
        String rawPassword = "admin123";
        String encodedPassword = "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi";
        
        // 验证密码是否匹配
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        assertTrue(matches, "密码验证应该成功");
        
        System.out.println("密码验证成功: admin123 匹配数据库中的哈希值");
    }
    
    @Test
    public void testGenerateNewHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        
        // 生成新的密码哈希用于测试
        String rawPassword = "admin123";
        String newHash = encoder.encode(rawPassword);
        
        System.out.println("新生成的密码哈希: " + newHash);
        
        // 验证新生成的哈希
        boolean matches = encoder.matches(rawPassword, newHash);
        assertTrue(matches, "新生成的哈希应该能正确验证密码");
    }
}
