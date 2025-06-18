package com.chatapp.message.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
@Slf4j
public class WebSocketUserSessionHandler implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String userId = accessor.getFirstNativeHeader("userId");
            String username = accessor.getFirstNativeHeader("username");
            
            log.info("WebSocket CONNECT: userId={}, username={}", userId, username);
            
            if (userId != null) {
                // 设置用户身份信息
                accessor.setUser(new CustomPrincipal(userId, username));
                log.info("设置用户身份: userId={}", userId);
            }
        }
        
        return message;
    }
    
    // 自定义Principal实现
    public static class CustomPrincipal implements Principal {
        private final String userId;
        private final String username;
        
        public CustomPrincipal(String userId, String username) {
            this.userId = userId;
            this.username = username;
        }
        
        @Override
        public String getName() {
            return userId;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getUsername() {
            return username;
        }
    }
} 