package com.chatapp.message.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.DefaultContentTypeResolver;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private WebSocketUserSessionHandler userSessionHandler;

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
        registry.addEndpoint("/ws-chat")
                .setAllowedOriginPatterns("*") // 允许所有源的跨域请求，生产环境应该限制
                .withSockJS(); // 启用SockJS回退
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 注册用户会话处理器
        registration.interceptors(userSessionHandler);
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
        // 配置自定义的消息转换器，使用我们配置的ObjectMapper
        DefaultContentTypeResolver resolver = new DefaultContentTypeResolver();
        resolver.setDefaultMimeType(MimeTypeUtils.APPLICATION_JSON);
        
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(objectMapper);
        converter.setContentTypeResolver(resolver);
        
        messageConverters.add(converter);
        return false; // 保留默认转换器
    }
} 