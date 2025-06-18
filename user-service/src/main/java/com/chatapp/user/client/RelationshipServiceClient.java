package com.chatapp.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "relationship-service")
public interface RelationshipServiceClient {

    @GetMapping("/api/v1/friendships/users/{friendId}/status")
    Map<String, String> getFriendshipStatus(@RequestHeader("X-User-ID") Long userId, @PathVariable Long friendId);
    
    @GetMapping("/api/v1/friendships/users/{friendId}/check")
    Map<String, Boolean> checkFriendship(@RequestHeader("X-User-ID") Long userId, @PathVariable Long friendId);
    
    @GetMapping("/api/v1/friendships/users/{friendId}/sender")
    Map<String, Boolean> checkRequestSender(@RequestHeader("X-User-ID") Long userId, @PathVariable Long friendId);
} 