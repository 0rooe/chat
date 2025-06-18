package com.chatapp.relationship.controller;

import com.chatapp.relationship.dto.FriendRequestDto;
import com.chatapp.relationship.dto.FriendshipDto;
import com.chatapp.relationship.model.Friendship;
import com.chatapp.relationship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FriendshipController {
    
    private final FriendshipService friendshipService;
    
    // 主API路径
    @PostMapping("/api/v1/friendships")
    public ResponseEntity<FriendshipDto> sendFriendRequest(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody FriendRequestDto friendRequestDto) {
        log.info("用户 {} 向用户 {} 发送好友请求", userId, friendRequestDto.getFriendId());
        FriendshipDto friendship = friendshipService.sendFriendRequest(userId, friendRequestDto);
        return new ResponseEntity<>(friendship, HttpStatus.CREATED);
    }
    
    // 兼容前端路径
    @PostMapping("/api/relationships/requests")
    public ResponseEntity<FriendshipDto> sendFriendRequestAlternative(
            @RequestHeader("X-User-ID") Long userId,
            @RequestBody Map<String, Long> requestMap) {
        Long receiverId = requestMap.get("receiverId");
        if (receiverId == null) {
            return ResponseEntity.badRequest().build();
        }
        log.info("前端路径: 用户 {} 向用户 {} 发送好友请求", userId, receiverId);
        FriendRequestDto friendRequestDto = new FriendRequestDto();
        friendRequestDto.setFriendId(receiverId);
        FriendshipDto friendship = friendshipService.sendFriendRequest(userId, friendRequestDto);
        return new ResponseEntity<>(friendship, HttpStatus.CREATED);
    }
    
    @PutMapping("/api/v1/friendships/requests/{requestId}/accept")
    public ResponseEntity<FriendshipDto> acceptFriendRequest(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long requestId) {
        log.info("用户 {} 接受好友请求 {}", userId, requestId);
        FriendshipDto friendship = friendshipService.acceptFriendRequest(userId, requestId);
        return ResponseEntity.ok(friendship);
    }
    
    // 兼容前端路径
    @PutMapping("/api/relationships/requests/{requestId}/accept")
    public ResponseEntity<FriendshipDto> acceptFriendRequestAlternative(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long requestId) {
        log.info("前端路径: 用户 {} 接受好友请求 {}", userId, requestId);
        return acceptFriendRequest(userId, requestId);
    }
    
    @DeleteMapping("/api/v1/friendships/requests/{requestId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long requestId) {
        log.info("用户 {} 拒绝好友请求 {}", userId, requestId);
        friendshipService.rejectFriendRequest(userId, requestId);
        return ResponseEntity.noContent().build();
    }
    
    // 兼容前端路径
    @DeleteMapping("/api/relationships/requests/{requestId}")
    public ResponseEntity<Void> rejectFriendRequestAlternative(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long requestId) {
        log.info("前端路径: 用户 {} 拒绝好友请求 {}", userId, requestId);
        return rejectFriendRequest(userId, requestId);
    }
    
    // 兼容前端路径
    @PutMapping("/api/relationships/requests/{requestId}/reject")
    public ResponseEntity<Void> rejectFriendRequestPut(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long requestId) {
        log.info("前端路径(PUT): 用户 {} 拒绝好友请求 {}", userId, requestId);
        return rejectFriendRequest(userId, requestId);
    }
    
    @PutMapping("/api/v1/friendships/users/{friendId}/block")
    public ResponseEntity<FriendshipDto> blockFriend(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("用户 {} 拉黑用户 {}", userId, friendId);
        FriendshipDto friendship = friendshipService.blockFriend(userId, friendId);
        return ResponseEntity.ok(friendship);
    }
    
    @PutMapping("/api/v1/friendships/users/{friendId}/unblock")
    public ResponseEntity<FriendshipDto> unblockFriend(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("用户 {} 解除拉黑用户 {}", userId, friendId);
        FriendshipDto friendship = friendshipService.unblockFriend(userId, friendId);
        return ResponseEntity.ok(friendship);
    }
    
    @DeleteMapping("/api/v1/friendships/users/{friendId}")
    public ResponseEntity<Void> deleteFriend(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("用户 {} 删除好友 {}", userId, friendId);
        friendshipService.deleteFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }
    
    @DeleteMapping("/api/v1/friendships/{friendshipId}")
    public ResponseEntity<Void> deleteFriendshipById(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendshipId) {
        log.info("用户 {} 通过关系ID {} 删除好友", userId, friendshipId);
        friendshipService.deleteFriendshipById(userId, friendshipId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/api/v1/friendships")
    public ResponseEntity<List<FriendshipDto>> getFriends(@RequestHeader("X-User-ID") Long userId) {
        log.info("获取用户 {} 的好友列表", userId);
        List<FriendshipDto> friends = friendshipService.getFriends(userId);
        return ResponseEntity.ok(friends);
    }
    
    // 兼容前端路径
    @GetMapping("/api/relationships/friends")
    public ResponseEntity<List<FriendshipDto>> getFriendsAlternative(@RequestHeader("X-User-ID") Long userId) {
        log.info("前端路径: 获取用户 {} 的好友列表", userId);
        return getFriends(userId);
    }
    
    @GetMapping("/api/v1/friendships/requests")
    public ResponseEntity<List<FriendshipDto>> getPendingRequests(@RequestHeader("X-User-ID") Long userId) {
        log.info("获取用户 {} 的待处理好友请求", userId);
        List<FriendshipDto> pendingRequests = friendshipService.getPendingRequests(userId);
        return ResponseEntity.ok(pendingRequests);
    }
    
    // 兼容前端路径
    @GetMapping("/api/relationships/requests/received")
    public ResponseEntity<List<FriendshipDto>> getPendingRequestsAlternative(@RequestHeader("X-User-ID") Long userId) {
        log.info("前端路径: 获取用户 {} 的待处理好友请求", userId);
        return getPendingRequests(userId);
    }
    
    // 标准API路径 - 获取发送的请求
    @GetMapping("/api/v1/friendships/requests/sent")
    public ResponseEntity<List<FriendshipDto>> getSentRequestsStandardPath(@RequestHeader("X-User-ID") Long userId) {
        log.info("标准路径: 获取用户 {} 的已发送好友请求", userId);
        List<FriendshipDto> sentRequests = friendshipService.getSentRequests(userId);
        return ResponseEntity.ok(sentRequests);
    }
    
    // 兼容前端路径 - 获取发送的请求
    @GetMapping("/api/relationships/requests/sent")
    public ResponseEntity<List<FriendshipDto>> getSentRequests(@RequestHeader("X-User-ID") Long userId) {
        log.info("前端路径: 获取用户 {} 的已发送好友请求", userId);
        return getSentRequestsStandardPath(userId);
    }
    
    @GetMapping("/api/v1/friendships/blocked")
    public ResponseEntity<List<FriendshipDto>> getBlockedUsers(@RequestHeader("X-User-ID") Long userId) {
        log.info("获取用户 {} 的已拉黑用户", userId);
        List<FriendshipDto> blockedUsers = friendshipService.getBlockedUsers(userId);
        return ResponseEntity.ok(blockedUsers);
    }
    
    @GetMapping("/api/v1/friendships/users/{friendId}/status")
    public ResponseEntity<Map<String, String>> getFriendshipStatus(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("获取用户 {} 和用户 {} 的好友关系状态", userId, friendId);
        Friendship.FriendshipStatus status = friendshipService.getFriendshipStatus(userId, friendId);
        
        String statusString = status != null ? status.name() : "NONE";
        Map<String, String> responseMap = new HashMap<>();
        responseMap.put("status", statusString);
        
        // 如果状态是PENDING，还需要检查是谁发送的请求
        if (status == Friendship.FriendshipStatus.PENDING) {
            boolean isSender = friendshipService.isRequestSender(userId, friendId);
            if (isSender) {
                responseMap.put("status", "PENDING_SENT");
            } else {
                responseMap.put("status", "PENDING_RECEIVED");
            }
        }
        
        return ResponseEntity.ok(responseMap);
    }
    
    @GetMapping("/api/v1/friendships/users/{friendId}/check")
    public ResponseEntity<Map<String, Boolean>> checkFriendship(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("检查用户 {} 和用户 {} 是否为好友", userId, friendId);
        boolean isFriend = friendshipService.isFriend(userId, friendId);
        return ResponseEntity.ok(Map.of("isFriend", isFriend));
    }
    
    @GetMapping("/api/v1/friendships/users/{friendId}/sender")
    public ResponseEntity<Map<String, Boolean>> checkRequestSender(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long friendId) {
        log.info("检查用户 {} 是否向用户 {} 发送了好友请求", userId, friendId);
        boolean isSender = friendshipService.isRequestSender(userId, friendId);
        return ResponseEntity.ok(Map.of("isSender", isSender));
    }
    
    // 添加测试端点
    @GetMapping("/api/v1/test/user/{userId}")
    public ResponseEntity<?> testGetUserInfo(@PathVariable Long userId) {
        log.info("测试获取用户信息: {}", userId);
        try {
            Map<String, Object> userInfo = friendshipService.testGetUserInfo(userId);
            log.info("测试成功: {}", userInfo);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("测试失败: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage(), "userId", userId));
        }
    }

} 