package com.chatapp.frontend.controller;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.chatapp.frontend.dto.UserResponseDto;
import com.chatapp.frontend.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    
    private final UserServiceClient userServiceClient;

    @GetMapping("/")
    public String home(HttpSession session) {
        log.info("【调试】访问首页，会话ID: {}", session.getId());
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String login(HttpSession session) {
        log.info("【调试】访问登录页面，会话ID: {}", session.getId());
        // 检查会话中是否已有用户信息
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user != null) {
            log.info("【调试】用户已登录，直接重定向到聊天页面: {}", user);
            return "redirect:/chat";
        }
        return "login";
    }
    
    @GetMapping("/register")
    public String register(HttpSession session) {
        log.info("【调试】访问注册页面，会话ID: {}", session.getId());
        return "register";
    }
    
    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        log.info("【调试】访问聊天页面，会话ID: {}", session.getId());
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        log.info("【调试】从会话中获取的用户信息: {}", user);
        
        if (user == null) {
            log.info("【调试】用户未登录，重定向到登录页面");
            return "redirect:/login";
        }
        
        try {
            model.addAttribute("user", user);
            log.info("【调试】已将用户信息添加到模型中: {}", user);
            // 输出模型中所有属性
            log.info("【调试】模型中的所有属性: {}", model.asMap());
            return "chat";
        } catch (Exception e) {
            log.error("【错误】处理聊天页面时发生异常: {}", e.getMessage(), e);
            return "redirect:/error";
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        log.info("【调试】用户注销，会话ID: {}", session.getId());
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        log.info("【调试】注销的用户: {}", user);
        
        // 在清除会话前，将用户状态设置为离线
        if (user != null && user.getId() != null) {
            try {
                userServiceClient.updateUserStatus(user.getId(), "OFFLINE");
                log.info("【调试】已将用户状态设置为离线: {}", user.getId());
            } catch (Exception e) {
                log.warn("【警告】设置用户离线状态失败: {}", e.getMessage());
                // 即使设置状态失败，也继续退出登录流程
            }
        }
        
        // 清除会话
        session.invalidate();
        log.info("【调试】会话已失效");
        
        return "redirect:/login?logout=true";
    }
} 