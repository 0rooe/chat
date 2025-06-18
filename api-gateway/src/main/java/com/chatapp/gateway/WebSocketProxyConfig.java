package com.chatapp.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketProxyConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // WebSocket路由到消息服务
            .route("websocket-route", r -> r
                .path("/ws-chat/**")
                .uri("lb://message-service"))
            .build();
    }
} 