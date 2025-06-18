package com.chatapp.message.service.impl;

import com.chatapp.message.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final RestTemplate restTemplate;
    
    // 用户信息缓存，避免频繁请求用户服务
    private final Map<Long, Map<String, Object>> userInfoCache = new ConcurrentHashMap<>();
    
    @Value("${services.user.url:http://user-service:8080}")
    private String userServiceUrl;
    
    @Override
    public Map<String, Object> getUserBasicInfo(Long userId) {
        // 先尝试从缓存获取
        if (userInfoCache.containsKey(userId)) {
            log.debug("从缓存获取用户信息: {}", userId);
            return userInfoCache.get(userId);
        }
        
        try {
            log.info("从用户服务获取用户基本信息: {}", userId);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-ID", String.valueOf(userId));
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            // 尝试先通过新接口获取
            String url = userServiceUrl + "/api/v1/users/" + userId + "/basic";
            ResponseEntity<Map> response;
            try {
                response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                
                if (response.getBody() != null) {
                    log.info("成功获取用户信息(v1): {}", response.getBody());
                    
                    // 缓存用户信息
                    userInfoCache.put(userId, response.getBody());
                    return response.getBody();
                }
            } catch (RestClientException e) {
                log.warn("通过v1接口获取用户信息失败: {}", e.getMessage());
                
                // 尝试通过兼容接口获取
                try {
                    url = userServiceUrl + "/api/users/" + userId;
                    response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                    
                    if (response.getBody() != null) {
                        log.info("成功获取用户信息(兼容): {}", response.getBody());
                        
                        // 缓存用户信息
                        userInfoCache.put(userId, response.getBody());
                        return response.getBody();
                    }
                } catch (RestClientException e2) {
                    log.error("通过兼容接口获取用户信息也失败: {}", e2.getMessage());
                }
            }
            
            // 如果所有接口都失败，使用默认值
            return createDefaultUserInfo(userId);
        } catch (Exception e) {
            log.error("获取用户信息异常: {}", e.getMessage(), e);
            return createDefaultUserInfo(userId);
        }
    }
    
    /**
     * 创建默认用户信息
     */
    private Map<String, Object> createDefaultUserInfo(Long userId) {
        log.info("创建用户 {} 的默认信息", userId);
        
        Map<String, Object> defaultInfo = new HashMap<>();
        defaultInfo.put("id", userId);
        defaultInfo.put("nickname", "用户" + userId);
        defaultInfo.put("username", "user" + userId);
        defaultInfo.put("status", "OFFLINE");
        defaultInfo.put("avatar", null);
        
        // 缓存默认信息
        userInfoCache.put(userId, defaultInfo);
        return defaultInfo;
    }
} 