package com.chatapp.message.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
@Slf4j
public class WebSocketEventListener {
    
    @Autowired
    private WebSocketUserRegistry userRegistry;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // 用户服务的基础URL
    private static final String USER_SERVICE_URL = "http://localhost:8081";

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        String userId = headerAccessor.getFirstNativeHeader("userId");
        String username = headerAccessor.getFirstNativeHeader("username");
        
        log.info("WebSocket连接中: sessionId={}, userId={}, username={}", sessionId, userId, username);
        
        // 将用户信息存储到会话属性中，供后续使用
        headerAccessor.getSessionAttributes().put("userId", userId);
        headerAccessor.getSessionAttributes().put("username", username);
        
        // 立即注册用户会话（在CONNECT事件中就注册）
        if (userId != null && sessionId != null) {
            userRegistry.addUserSession(userId, sessionId);
            log.info("用户会话已注册: userId={}, sessionId={}", userId, sessionId);
        }
    }
    
    @EventListener
    public void handleWebSocketConnectedListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        Object userId = null;
        Object username = null;
        
        // 安全地获取会话属性
        if (headerAccessor.getSessionAttributes() != null) {
            userId = headerAccessor.getSessionAttributes().get("userId");
            username = headerAccessor.getSessionAttributes().get("username");
        }
        
        // 将用户会话关系注册到注册表
        if (userId != null) {
            userRegistry.addUserSession(userId.toString(), sessionId);
            
            // 自动设置用户为在线状态
            updateUserStatus(userId.toString(), "ONLINE");
        }
        
        log.info("WebSocket连接已建立: sessionId={}, userId={}, username={}", sessionId, userId, username);
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        // 获取用户ID（从会话属性或注册表中）
        String userId = null;
        if (headerAccessor.getSessionAttributes() != null) {
            Object userIdObj = headerAccessor.getSessionAttributes().get("userId");
            if (userIdObj != null) {
                userId = userIdObj.toString();
            }
        }
        
        // 如果没有从会话属性获取到，尝试从注册表获取
        if (userId == null) {
            userId = userRegistry.getSessionUser(sessionId);
        }
        
        // 从注册表中移除会话
        userRegistry.removeSession(sessionId);
        
        // 检查用户是否还有其他活跃会话，如果没有则设置为离线
        if (userId != null && !userRegistry.isUserOnline(userId)) {
            updateUserStatus(userId, "OFFLINE");
            log.info("用户已离线: userId={}", userId);
        }
        
        log.info("WebSocket连接断开: sessionId={}, userId={}", sessionId, userId);
    }
    
    /**
     * 更新用户状态
     */
    private void updateUserStatus(String userId, String status) {
        try {
            String url = USER_SERVICE_URL + "/api/v1/users/" + userId + "/status";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("status", status);
            
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
            
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
            log.info("自动更新用户状态成功: userId={}, status={}", userId, status);
            
        } catch (Exception e) {
            log.error("自动更新用户状态失败: userId={}, status={}, error={}", userId, status, e.getMessage());
        }
    }
} 