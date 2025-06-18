package com.chatapp.message.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Iterator;

@Component
@Slf4j
public class WebSocketUserRegistry {
    
    // 用户ID -> 会话ID的映射
    private final ConcurrentMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // 会话ID -> 用户ID的映射
    private final ConcurrentMap<String, String> sessionUsers = new ConcurrentHashMap<>();
    
    // 用户ID -> 最后活跃时间的映射
    private final ConcurrentMap<String, LocalDateTime> userLastActive = new ConcurrentHashMap<>();
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String USER_SERVICE_URL = "http://localhost:8081";
    
    // 用户离线超时时间（分钟）
    private static final int OFFLINE_TIMEOUT_MINUTES = 5;
    
    public void addUserSession(String userId, String sessionId) {
        log.info("添加用户会话映射: userId={}, sessionId={}", userId, sessionId);
        userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(sessionId);
        sessionUsers.put(sessionId, userId);
        
        // 更新最后活跃时间
        updateUserLastActive(userId);
    }
    
    public void removeSession(String sessionId) {
        String userId = sessionUsers.remove(sessionId);
        if (userId != null) {
            log.info("移除用户会话映射: userId={}, sessionId={}", userId, sessionId);
            Set<String> sessions = userSessions.get(userId);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userSessions.remove(userId);
                    // 用户所有会话都断开时，移除最后活跃时间记录
                    userLastActive.remove(userId);
                }
            }
        }
    }
    
    public Set<String> getUserSessions(String userId) {
        return userSessions.getOrDefault(userId, Set.of());
    }
    
    public String getSessionUser(String sessionId) {
        return sessionUsers.get(sessionId);
    }
    
    public boolean isUserOnline(String userId) {
        Set<String> sessions = userSessions.get(userId);
        return sessions != null && !sessions.isEmpty();
    }
    
    /**
     * 更新用户最后活跃时间
     */
    public void updateUserLastActive(String userId) {
        userLastActive.put(userId, LocalDateTime.now());
        log.debug("更新用户最后活跃时间: userId={}, time={}", userId, LocalDateTime.now());
    }
    
    /**
     * 获取所有在线用户
     */
    public Set<String> getAllOnlineUsers() {
        return userSessions.keySet();
    }
    
    /**
     * 定时任务：检查并清理长时间未活跃的用户
     * 每2分钟执行一次
     */
    @Scheduled(fixedRate = 120000) // 2分钟
    public void cleanupInactiveUsers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(OFFLINE_TIMEOUT_MINUTES);
        
        Iterator<Map.Entry<String, LocalDateTime>> iterator = userLastActive.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, LocalDateTime> entry = iterator.next();
            String userId = entry.getKey();
            LocalDateTime lastActive = entry.getValue();
            
            // 如果用户超过5分钟未活跃
            if (lastActive.isBefore(cutoffTime)) {
                log.info("检测到用户长时间未活跃: userId={}, lastActive={}", userId, lastActive);
                
                // 移除所有会话
                Set<String> sessions = userSessions.get(userId);
                if (sessions != null) {
                    for (String sessionId : sessions) {
                        sessionUsers.remove(sessionId);
                    }
                    userSessions.remove(userId);
                }
                
                // 移除活跃时间记录
                iterator.remove();
                
                // 设置用户为离线状态
                updateUserStatus(userId, "OFFLINE");
                
                log.info("已清理非活跃用户: userId={}", userId);
            }
        }
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