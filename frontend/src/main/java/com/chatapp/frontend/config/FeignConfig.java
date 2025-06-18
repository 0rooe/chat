package com.chatapp.frontend.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor jwtRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 为所有请求设置默认的Content-Type
                if (!template.headers().containsKey("Content-Type")) {
                    template.header("Content-Type", "application/json");
                }
                
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpSession session = attributes.getRequest().getSession(false);
                    if (session != null) {
                        String jwtToken = (String) session.getAttribute("jwtToken");
                        
                        // 为需要JWT认证的服务添加Authorization头
                        if (template.url().contains("user-service")) {
                            if (jwtToken != null && !jwtToken.trim().isEmpty()) {
                                template.header("Authorization", "Bearer " + jwtToken);
                                log.debug("【Feign拦截器】为user-service请求添加JWT认证头, URL: {}", template.url());
                            } else {
                                log.warn("【Feign拦截器】user-service请求缺少JWT令牌，URL: {}, token状态: {}", 
                                        template.url(), jwtToken == null ? "null" : "empty");
                                // 对于状态更新等内部调用，可以考虑跳过认证或使用其他方式
                                if (template.url().contains("/status")) {
                                    log.info("【Feign拦截器】状态更新请求，尝试继续执行（依赖服务端配置）");
                                }
                            }
                        }
                        
                        // 为relationship-service添加用户ID头和JWT认证头（如果需要）
                        if (template.url().contains("relationship-service")) {
                            // 添加用户ID头
                            Object userObj = session.getAttribute("user");
                            if (userObj != null) {
                                try {
                                    // 假设user对象有getId()方法
                                    Long userId = ((com.chatapp.frontend.dto.UserResponseDto) userObj).getId();
                                    template.header("X-User-ID", String.valueOf(userId));
                                    log.debug("【Feign拦截器】为relationship-service请求添加用户ID头: {}", userId);
                                    
                                    // 同时也添加JWT token，以防relationship-service需要
                                    if (jwtToken != null) {
                                        template.header("Authorization", "Bearer " + jwtToken);
                                        log.debug("【Feign拦截器】为relationship-service请求添加JWT认证头");
                                    }
                                } catch (Exception e) {
                                    log.error("【Feign拦截器】获取用户ID失败: {}", e.getMessage());
                                }
                            } else {
                                log.warn("【Feign拦截器】会话中未找到用户信息");
                            }
                        }
                        
                        // 为message-service添加认证支持
                        if (template.url().contains("message-service")) {
                            Object userObj = session.getAttribute("user");
                            if (userObj != null) {
                                try {
                                    Long userId = ((com.chatapp.frontend.dto.UserResponseDto) userObj).getId();
                                    template.header("X-User-ID", String.valueOf(userId));
                                    log.debug("【Feign拦截器】为message-service请求添加用户ID头: {}", userId);
                                } catch (Exception e) {
                                    log.error("【Feign拦截器】获取用户ID失败: {}", e.getMessage());
                                }
                            }
                            
                            if (jwtToken != null) {
                                template.header("Authorization", "Bearer " + jwtToken);
                                log.debug("【Feign拦截器】为message-service请求添加JWT认证头");
                            }
                        }
                    } else {
                        log.warn("【Feign拦截器】会话为空，无法添加认证信息");
                    }
                } else {
                    log.warn("【Feign拦截器】请求上下文为空，无法添加认证信息");
                }
                
                // 记录最终的请求头信息（调试用）
                log.debug("【Feign拦截器】最终请求头: {}", template.headers());
            }
        };
    }
} 