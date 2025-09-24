package com.testplatform.service;

import com.testplatform.model.User;
import com.testplatform.model.dto.LoginRequest;
import com.testplatform.model.dto.RegisterRequest;
import com.testplatform.repository.UserRepository;
import com.testplatform.util.JwtUtil;
import com.testplatform.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 用户服务类
 * 处理用户认证、注册、管理等功能
 */
@Service
@Primary
@Transactional
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordUtil passwordUtil;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录
     */
    public User login(LoginRequest loginRequest) {
        User user = findByUsername(loginRequest.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!passwordUtil.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("账户已被禁用");
        }

        // 更新最后登录时间
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    /**
     * 用户注册
     */
    public User register(RegisterRequest registerRequest) {
        // 验证密码确认
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        if (findByUsername(registerRequest.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (findByEmail(registerRequest.getEmail()) != null) {
            throw new RuntimeException("邮箱已被使用");
        }

        // 验证密码强度
        if (!passwordUtil.isStrongPassword(registerRequest.getPassword())) {
            throw new RuntimeException("密码强度不够，请包含大小写字母、数字和特殊字符");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPasswordHash(passwordUtil.encodePassword(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole("USER");
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * 根据邮箱查找用户
     */
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    /**
     * 根据ID查找用户
     */
    public User findById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * 更新用户信息
     */
    public User updateUser(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    /**
     * 修改密码
     */
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordUtil.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }

        if (!passwordUtil.isStrongPassword(newPassword)) {
            throw new RuntimeException("新密码强度不够，请包含大小写字母、数字和特殊字符");
        }

        user.setPasswordHash(passwordUtil.encodePassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 重置密码
     */
    public String resetPassword(String email) {
        User user = findByEmail(email);
        if (user == null) {
            throw new RuntimeException("邮箱不存在");
        }

        String newPassword = passwordUtil.generateRandomPassword(12);
        user.setPasswordHash(passwordUtil.encodePassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        return newPassword;
    }

    /**
     * 启用/禁用用户
     */
    public void toggleUserStatus(String userId) {
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        user.setIsActive(!user.getIsActive());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    /**
     * 删除用户
     */
    public void deleteUser(String userId) {
        User user = findById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        userRepository.delete(user);
    }

    /**
     * 实现UserDetailsService接口
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .authorities(user.getRole())
                .accountExpired(false)
                .accountLocked(!user.getIsActive())
                .credentialsExpired(false)
                .disabled(!user.getIsActive())
                .build();
    }
}
