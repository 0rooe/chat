package com.chatapp.message.controller;

import com.chatapp.message.dto.ConversationRequest;
import com.chatapp.message.dto.ConversationResponse;
import com.chatapp.message.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 会话控制器
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 创建或获取会话
     */
    @PostMapping("/api/messages/conversations")
    public ResponseEntity<ConversationResponse> createOrGetConversation(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody ConversationRequest request) {
        
        log.info("用户 {} 创建或获取与用户 {} 的会话", userId, request.getRecipientId());
        
        ConversationResponse conversation = conversationService.createOrGetConversation(userId, request.getRecipientId());
        return new ResponseEntity<>(conversation, HttpStatus.CREATED);
    }
    
    /**
     * 获取用户的所有会话
     */
    @GetMapping("/api/messages/conversations")
    public ResponseEntity<List<ConversationResponse>> getUserConversations(
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("获取用户 {} 的所有会话", userId);
        
        List<ConversationResponse> conversations = conversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }
    
    /**
     * 获取特定会话
     */
    @GetMapping("/api/messages/conversations/{conversationId}")
    public ResponseEntity<ConversationResponse> getConversation(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable String conversationId) {
        
        log.info("用户 {} 获取会话 {}", userId, conversationId);
        
        ConversationResponse conversation = conversationService.getConversation(conversationId, userId);
        return ResponseEntity.ok(conversation);
    }
    
    /**
     * 删除会话
     */
    @DeleteMapping("/api/messages/conversations/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable String conversationId) {
        
        log.info("用户 {} 删除会话 {}", userId, conversationId);
        
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
} 