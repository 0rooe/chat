package com.chatapp.notification.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，用于将消息广播到客户端
        registry.enableSimpleBroker("/topic", "/queue");
        
        // 指定消息被发送到的前缀
        registry.setApplicationDestinationPrefixes("/app");
        
        // 配置用户目的地前缀，用于点对点消息
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册STOMP端点，用于客户端连接
        registry.addEndpoint("/ws-notifications")
                .setAllowedOriginPatterns("*") // 允许所有源的跨域请求，生产环境应该限制
                .withSockJS(); // 启用SockJS回退
    }
} 