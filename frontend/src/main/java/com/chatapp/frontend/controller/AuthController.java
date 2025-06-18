package com.chatapp.frontend.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.chatapp.frontend.client.UserServiceClient;
import com.chatapp.frontend.dto.UserRegistrationDto;
import com.chatapp.frontend.dto.UserResponseDto;
import com.chatapp.frontend.dto.LoginRequest;
import com.chatapp.frontend.dto.ApiResponse;
import com.chatapp.frontend.dto.JwtResponseDto;

import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Controller
@Slf4j
public class AuthController {

    @Autowired
    private UserServiceClient userServiceClient;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserRegistrationDto registrationDto, 
                              RedirectAttributes redirectAttributes) {
        try {
            log.info("接收到注册请求: {}", registrationDto);
            
            // 调用用户服务进行注册
            ResponseEntity<ApiResponse> response = userServiceClient.registerUser(registrationDto);
            log.info("用户服务注册响应状态码: {}, 响应体: {}", response.getStatusCode(), response.getBody());
            
            // 注册成功
            log.info("用户注册成功: {}", response.getBody());
            redirectAttributes.addAttribute("success", true);
            return "redirect:/login";
            
        } catch (Exception e) {
            log.error("注册过程中发生错误: {}", e.getMessage(), e);
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "注册失败: " + e.getMessage());
            return "redirect:/register";
        }
    }
    
    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequest loginRequest,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        try {
            log.info("【调试】接收到登录请求: {}", loginRequest);
            
            // 调用用户服务进行登录
            log.info("【调试】准备调用用户服务进行登录");
            ResponseEntity<JwtResponseDto> response = userServiceClient.loginUser(loginRequest);
            log.info("【调试】用户服务登录响应状态码: {}", response.getStatusCode());
            log.info("【调试】用户服务登录响应内容: {}", response.getBody());
            
            if (response.getBody() != null) {
                JwtResponseDto jwtResponse = response.getBody();
                log.info("【调试】获取到JWT响应: {}", jwtResponse);
                
                // 创建UserResponseDto对象
                UserResponseDto user = new UserResponseDto();
                user.setId(jwtResponse.getId());
                user.setUsername(jwtResponse.getUsername());
                // 如果没有昵称，使用用户名
                user.setNickname(jwtResponse.getUsername());
                
                log.info("【调试】创建的UserResponseDto对象: {}", user);
                
                // 存储用户信息和JWT令牌到会话
                session.setAttribute("user", user);
                session.setAttribute("jwtToken", jwtResponse.getToken());
                log.info("【调试】用户信息和JWT令牌已存储到会话，会话ID: {}", session.getId());
                
                // 检查会话中是否正确存储了用户信息
                UserResponseDto userInSession = (UserResponseDto) session.getAttribute("user");
                log.info("【调试】从会话中检索的用户信息: {}", userInSession);
                
                // 登录成功后重定向到聊天页面
                log.info("【调试】登录成功，准备重定向到聊天页面");
                return "redirect:/chat";
            } else {
                log.info("【调试】响应体为空");
                redirectAttributes.addAttribute("error", true);
                redirectAttributes.addAttribute("message", "登录失败，服务器返回空响应");
                return "redirect:/login";
            }
        } catch (Exception e) {
            log.error("【错误】登录过程中发生错误: {}", e.getMessage(), e);
            redirectAttributes.addAttribute("error", true);
            redirectAttributes.addAttribute("message", "登录失败: " + e.getMessage());
            return "redirect:/login";
        }
    }
} 