package com.chatapp.user.controller;

import com.chatapp.user.model.User;
import com.chatapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @GetMapping("/user/{username}")
    public String checkUser(@PathVariable String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return String.format("用户存在: %s, 密码长度: %d, 密码前缀: %s", 
                username, user.getPassword().length(), user.getPassword().substring(0, Math.min(10, user.getPassword().length())));
        } else {
            return "用户不存在: " + username;
        }
    }
    
    @PostMapping("/password-check")
    public String checkPassword(@RequestParam String username, @RequestParam String rawPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());
            return String.format("用户: %s, 原始密码: %s, 加密密码: %s, 匹配结果: %s", 
                username, rawPassword, user.getPassword(), matches);
        } else {
            return "用户不存在: " + username;
        }
    }
    
    @PostMapping("/encode-password")
    public String encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return String.format("原始密码: %s, 加密后: %s", password, encoded);
    }
} 