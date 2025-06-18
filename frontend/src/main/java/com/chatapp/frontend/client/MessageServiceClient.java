package com.chatapp.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "message-service")
public interface MessageServiceClient {

    /**
     * 获取最近的聊天列表
     */
    @GetMapping("/api/v1/chats/recent")
    ResponseEntity<List<Map<String, Object>>> getRecentChats(@RequestHeader("X-User-ID") Long userId);
    
    /**
     * 获取私聊历史消息
     */
    @GetMapping("/api/v1/messages/private/list")
    ResponseEntity<List<Map<String, Object>>> getPrivateMessages(
            @RequestParam("userId") Long userId,
            @RequestParam("friendId") Long friendId);
    
    /**
     * 获取群聊历史消息
     */
    @GetMapping("/api/v1/messages/group/{groupId}/list")
    ResponseEntity<List<Map<String, Object>>> getGroupMessages(
            @PathVariable("groupId") Long groupId);
    
    /**
     * 获取用户未读消息
     */
    @GetMapping("/api/v1/messages/unread/{userId}")
    ResponseEntity<List<Map<String, Object>>> getUnreadMessages(
            @PathVariable("userId") Long userId);
    
    /**
     * 标记消息为已读
     */
    @PostMapping("/api/v1/messages/mark-read")
    ResponseEntity<Integer> markMessagesAsRead(@RequestBody Map<String, Long> request);
    
    /**
     * 标记所有消息为已读
     */
    @PostMapping("/api/v1/messages/mark-all-read/{userId}")
    ResponseEntity<Integer> markAllMessagesAsRead(@PathVariable("userId") Long userId);
} 