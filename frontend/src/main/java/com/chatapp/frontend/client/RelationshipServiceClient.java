package com.chatapp.frontend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chatapp.frontend.dto.FriendRequestDto;

import java.util.List;
import java.util.Map;

@FeignClient(name = "relationship-service")
public interface RelationshipServiceClient {

    // 发送好友请求
    @PostMapping(value = "/api/v1/friendships", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> sendFriendRequest(@RequestHeader("X-User-ID") Long userId, @RequestBody FriendRequestDto request);
    
    // 接受好友请求
    @PutMapping(value = "/api/v1/friendships/requests/{requestId}/accept", produces = "application/json")
    ResponseEntity<?> acceptFriendRequest(@RequestHeader("X-User-ID") Long userId, @PathVariable Long requestId);
    
    // 拒绝好友请求
    @DeleteMapping(value = "/api/v1/friendships/requests/{requestId}", produces = "application/json")
    ResponseEntity<?> rejectFriendRequest(@RequestHeader("X-User-ID") Long userId, @PathVariable Long requestId);
    
    // 获取好友列表
    @GetMapping(value = "/api/v1/friendships", produces = "application/json")
    ResponseEntity<List<?>> getFriends(@RequestHeader("X-User-ID") Long userId);
    
    // 获取待处理的好友请求
    @GetMapping(value = "/api/v1/friendships/requests", produces = "application/json")
    ResponseEntity<List<?>> getPendingRequests(@RequestHeader("X-User-ID") Long userId);
    
    // 获取已发送的好友请求
    @GetMapping(value = "/api/v1/friendships/requests/sent", produces = "application/json")
    ResponseEntity<List<?>> getSentRequests(@RequestHeader("X-User-ID") Long userId);
    
    // 获取已拉黑的用户
    @GetMapping(value = "/api/v1/friendships/blocked", produces = "application/json")
    ResponseEntity<List<?>> getBlockedUsers(@RequestHeader("X-User-ID") Long userId);
    
    // 删除好友关系
    @DeleteMapping(value = "/api/v1/friendships/{friendshipId}", produces = "application/json")
    ResponseEntity<?> deleteFriendship(@RequestHeader("X-User-ID") Long userId, @PathVariable Long friendshipId);
    
    // 群组相关API
    @GetMapping(value = "/api/v1/groups/users/{userId}", produces = "application/json")
    ResponseEntity<List<?>> getUserGroups(@PathVariable Long userId);
    
    @PostMapping(value = "/api/v1/groups", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> createGroup(@RequestHeader("X-User-ID") Long userId, @RequestBody java.util.Map<String, Object> groupData);
    
    @PostMapping(value = "/api/v1/groups/{groupId}/join", produces = "application/json")
    ResponseEntity<?> joinGroup(@RequestHeader("X-User-ID") Long userId, @PathVariable Long groupId);
    
    @DeleteMapping(value = "/api/v1/groups/{groupId}/leave", produces = "application/json")
    ResponseEntity<?> leaveGroup(@RequestHeader("X-User-ID") Long userId, @PathVariable Long groupId);
    
    @DeleteMapping(value = "/api/v1/groups/{groupId}", produces = "application/json")
    ResponseEntity<?> deleteGroup(@RequestHeader("X-User-ID") Long userId, @PathVariable Long groupId);
    
    @GetMapping(value = "/api/v1/groups/search", produces = "application/json")
    ResponseEntity<?> searchGroups(@RequestParam("name") String name);
    
    // 群公告相关API
    @PostMapping(value = "/api/v1/groups/{groupId}/announcement", consumes = "application/json", produces = "application/json")
    ResponseEntity<?> publishGroupAnnouncement(@PathVariable Long groupId, @RequestBody Map<String, String> announcementData, @RequestHeader("X-User-ID") Long userId);
    
    @GetMapping(value = "/api/v1/groups/{groupId}/announcement/latest", produces = "application/json")
    ResponseEntity<?> getLatestGroupAnnouncement(@PathVariable Long groupId);
} 