package com.chatapp.relationship.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}/basic")
    Map<String, Object> getUserBasicInfo(
        @PathVariable Long userId,
        @RequestHeader(value = "X-User-ID", required = false) Long requestUserId);
} 