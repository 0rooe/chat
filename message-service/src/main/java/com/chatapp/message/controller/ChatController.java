package com.chatapp.message.controller;

import com.chatapp.message.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取用户最近的聊天列表
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Map<String, Object>>> getRecentChats(@RequestHeader("X-User-ID") Long userId) {
        log.info("获取用户 {} 的最近聊天列表", userId);
        
        try {
            List<Map<String, Object>> recentChats = chatService.getRecentChats(userId);
            return ResponseEntity.ok(recentChats);
        } catch (Exception e) {
            log.error("获取用户最近聊天列表失败: {}", e.getMessage(), e);
            // 返回空列表避免前端出错
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
} 