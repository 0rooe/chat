package com.chatapp.frontend.controller;

import com.chatapp.frontend.client.UserServiceClient;
import com.chatapp.frontend.client.RelationshipServiceClient;
import com.chatapp.frontend.client.MessageServiceClient;
import com.chatapp.frontend.dto.FriendRequestDto;
import com.chatapp.frontend.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ApiProxyController {
    
    private final UserServiceClient userServiceClient;
    private final RelationshipServiceClient relationshipServiceClient;
    private final MessageServiceClient messageServiceClient;
    
    @GetMapping("/users/search")
    public ResponseEntity<List<UserResponseDto>> searchUsers(@RequestParam String query, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("搜索用户失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        String jwtToken = (String) session.getAttribute("jwtToken");
        if (jwtToken == null) {
            log.error("搜索用户失败：JWT令牌不存在");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("前端服务接收到用户搜索请求，查询条件: {}, 当前用户ID: {}", query, user.getId());
        try {
            String authHeader = "Bearer " + jwtToken;
            ResponseEntity<List<UserResponseDto>> response = userServiceClient.searchUsers(query, user.getId(), authHeader);
            log.info("从用户服务获取到搜索结果，结果数量: {}", 
                    response.getBody() != null ? response.getBody().size() : 0);
            return response;
        } catch (Exception e) {
            log.error("用户搜索请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // 好友相关API代理
    
    @PostMapping("/relationships/requests")
    public ResponseEntity<?> sendFriendRequest(@RequestBody Map<String, Long> requestMap, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("发送好友请求失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        Long receiverId = requestMap.get("receiverId");
        if (receiverId == null) {
            log.error("发送好友请求失败：缺少接收者ID");
            return ResponseEntity.badRequest().body(Map.of("error", "缺少接收者ID"));
        }
        
        log.info("发送好友请求：发送者ID={}, 接收者ID={}", user.getId(), receiverId);
        try {
            FriendRequestDto requestDto = new FriendRequestDto();
            requestDto.setFriendId(receiverId);
            
            return relationshipServiceClient.sendFriendRequest(user.getId(), requestDto);
        } catch (Exception e) {
            log.error("发送好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "发送好友请求失败"));
        }
    }
    
    @PutMapping("/relationships/requests/{requestId}/accept")
    public ResponseEntity<?> acceptFriendRequest(@PathVariable Long requestId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("接受好友请求失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("接受好友请求：用户ID={}, 请求ID={}", user.getId(), requestId);
        try {
            return relationshipServiceClient.acceptFriendRequest(user.getId(), requestId);
        } catch (Exception e) {
            log.error("接受好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "接受好友请求失败"));
        }
    }
    
    @PutMapping("/relationships/requests/{requestId}/reject")
    public ResponseEntity<?> rejectFriendRequest(@PathVariable Long requestId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("拒绝好友请求失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("拒绝好友请求：用户ID={}, 请求ID={}", user.getId(), requestId);
        try {
            return relationshipServiceClient.rejectFriendRequest(user.getId(), requestId);
        } catch (Exception e) {
            log.error("拒绝好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "拒绝好友请求失败"));
        }
    }
    
    @DeleteMapping("/relationships/requests/{requestId}")
    public ResponseEntity<?> cancelFriendRequest(@PathVariable Long requestId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("取消好友请求失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("取消好友请求：用户ID={}, 请求ID={}", user.getId(), requestId);
        try {
            return relationshipServiceClient.rejectFriendRequest(user.getId(), requestId);
        } catch (Exception e) {
            log.error("取消好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "取消好友请求失败"));
        }
    }
    
    @GetMapping("/relationships/friends")
    public ResponseEntity<List<?>> getFriends(HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取好友列表失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取好友列表：用户ID={}", user.getId());
        try {
            return relationshipServiceClient.getFriends(user.getId());
        } catch (Exception e) {
            log.error("获取好友列表失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/relationships/requests/received")
    public ResponseEntity<List<?>> getReceivedRequests(HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取收到的好友请求失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取收到的好友请求：用户ID={}", user.getId());
        try {
            return relationshipServiceClient.getPendingRequests(user.getId());
        } catch (Exception e) {
            log.error("获取收到的好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/relationships/requests/sent")
    public ResponseEntity<List<?>> getSentRequests(HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取发送的好友请求失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取发送的好友请求：用户ID={}", user.getId());
        try {
            // 使用正确的方法获取发送的请求
            return relationshipServiceClient.getSentRequests(user.getId());
        } catch (Exception e) {
            log.error("获取发送的好友请求失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/relationships/{friendshipId}")
    public ResponseEntity<?> deleteFriend(@PathVariable Long friendshipId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("删除好友失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("删除好友：用户ID={}, 好友关系ID={}", user.getId(), friendshipId);
        try {
            return relationshipServiceClient.deleteFriendship(user.getId(), friendshipId);
        } catch (Exception e) {
            log.error("删除好友失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "删除好友失败"));
        }
    }
    
    // 群组相关API代理
    
    @GetMapping("/v1/groups/users/{userId}")
    public ResponseEntity<?> getUserGroups(@PathVariable Long userId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null || !user.getId().equals(userId)) {
            log.error("获取用户群组失败：用户未登录或ID不匹配");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录或ID不匹配"));
        }
        
        log.info("获取用户群组：用户ID={}", userId);
        try {
            // 直接调用relationship-service的群组API
            return relationshipServiceClient.getUserGroups(userId);
        } catch (Exception e) {
            log.error("获取用户群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取用户群组失败"));
        }
    }
    
    @PostMapping("/v1/groups")
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> groupData, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("创建群组失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("创建群组：用户ID={}, 群组数据={}", user.getId(), groupData);
        try {
            return relationshipServiceClient.createGroup(user.getId(), groupData);
        } catch (Exception e) {
            log.error("创建群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "创建群组失败"));
        }
    }
    
    @PostMapping("/v1/groups/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("加入群组失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("加入群组：用户ID={}, 群组ID={}", user.getId(), groupId);
        try {
            return relationshipServiceClient.joinGroup(user.getId(), groupId);
        } catch (Exception e) {
            log.error("加入群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "加入群组失败"));
        }
    }
    
    @DeleteMapping("/v1/groups/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("退出群组失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("退出群组：用户ID={}, 群组ID={}", user.getId(), groupId);
        try {
            return relationshipServiceClient.leaveGroup(user.getId(), groupId);
        } catch (Exception e) {
            log.error("退出群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "退出群组失败"));
        }
    }
    
    @DeleteMapping("/v1/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Long groupId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("删除群组失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("删除群组：用户ID={}, 群组ID={}", user.getId(), groupId);
        try {
            return relationshipServiceClient.deleteGroup(user.getId(), groupId);
        } catch (Exception e) {
            log.error("删除群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "删除群组失败"));
        }
    }
    
    @GetMapping("/v1/groups/search")
    public ResponseEntity<?> searchGroups(@RequestParam String name, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("搜索群组失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("搜索群组：用户ID={}, 搜索词={}", user.getId(), name);
        try {
            return relationshipServiceClient.searchGroups(name);
        } catch (Exception e) {
            log.error("搜索群组失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "搜索群组失败"));
        }
    }

    @PostMapping("/v1/groups/{groupId}/announcement")
    public ResponseEntity<?> publishGroupAnnouncement(@PathVariable Long groupId, 
                                                    @RequestBody Map<String, String> announcementData, 
                                                    HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("发布群公告失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        String title = announcementData.get("title");
        String content = announcementData.get("content");
        
        if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
            log.error("发布群公告失败：标题或内容为空");
            return ResponseEntity.badRequest().body(Map.of("error", "标题和内容不能为空"));
        }
        
        log.info("发布群公告：用户ID={}, 群组ID={}, 标题={}", user.getId(), groupId, title);
        
        try {
            return relationshipServiceClient.publishGroupAnnouncement(groupId, announcementData, user.getId());
        } catch (Exception e) {
            log.error("发布群公告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "发布群公告失败"));
        }
    }
    
    @GetMapping("/v1/groups/{groupId}/announcement/latest")
    public ResponseEntity<?> getLatestGroupAnnouncement(@PathVariable Long groupId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取群公告失败：用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("获取群组最新公告：用户ID={}, 群组ID={}", user.getId(), groupId);
        try {
            return relationshipServiceClient.getLatestGroupAnnouncement(groupId);
        } catch (Exception e) {
            log.error("获取群公告失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", "获取群公告失败"));
        }
    }

    // 消息相关API代理
    
    @GetMapping("/chats/recent")
    public ResponseEntity<?> getRecentChats(HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取最近聊天列表失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取最近聊天列表：用户ID={}", user.getId());
        try {
            return messageServiceClient.getRecentChats(user.getId());
        } catch (Exception e) {
            log.error("获取最近聊天列表失败: {}", e.getMessage(), e);
            // 返回空列表而不是错误状态，避免前端出错
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/messages/private")
    public ResponseEntity<?> getPrivateMessages(
            @RequestParam Long userId,
            @RequestParam Long friendId,
            HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null || !user.getId().equals(userId)) {
            log.error("获取私聊消息失败：用户未登录或ID不匹配");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取私聊消息：用户ID={}, 好友ID={}", userId, friendId);
        try {
            return messageServiceClient.getPrivateMessages(userId, friendId);
        } catch (Exception e) {
            log.error("获取私聊消息失败: {}", e.getMessage(), e);
            // 返回空列表而不是错误状态
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/messages/group/{groupId}")
    public ResponseEntity<?> getGroupMessages(@PathVariable Long groupId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("获取群聊消息失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取群聊消息：用户ID={}, 群组ID={}", user.getId(), groupId);
        try {
            return messageServiceClient.getGroupMessages(groupId);
        } catch (Exception e) {
            log.error("获取群聊消息失败: {}", e.getMessage(), e);
            // 返回空列表而不是错误状态
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable Long userId, HttpSession session) {
        UserResponseDto currentUser = (UserResponseDto) session.getAttribute("user");
        if (currentUser == null) {
            log.error("获取用户信息失败：当前用户未登录");
            return ResponseEntity.badRequest().body(Map.of("error", "用户未登录"));
        }
        
        log.info("获取用户信息：用户ID={}", userId);
        try {
            // 如果是当前用户，直接返回会话中的信息
            if (currentUser.getId().equals(userId)) {
                log.info("返回当前用户信息: {}", currentUser);
                return ResponseEntity.ok(currentUser);
            }
            
            // 尝试从用户服务直接获取用户信息（使用基本信息端点，不需要认证）
            log.info("尝试从用户服务获取用户ID={}", userId);
            try {
                ResponseEntity<UserResponseDto> userResponse = userServiceClient.getUserById(userId);
                if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                    log.info("成功从用户服务获取到用户信息: {}", userResponse.getBody());
                    return userResponse;
                } else {
                    log.warn("用户服务返回空或错误响应");
                }
            } catch (Exception e) {
                log.warn("从用户服务获取用户信息失败: {}", e.getMessage());
                // 失败后尝试从好友关系中查找
            }
            
            // 如果用户服务查询失败，尝试从好友关系中查找
            log.info("尝试从好友关系中查找用户ID={}", userId);
            ResponseEntity<List<?>> friendsResponse = relationshipServiceClient.getFriends(currentUser.getId());
            if (friendsResponse.getStatusCode().is2xxSuccessful() && friendsResponse.getBody() != null) {
                List<?> friends = friendsResponse.getBody();
                log.info("获取到 {} 个好友关系", friends.size());
                
                for (Object friend : friends) {
                    if (friend instanceof Map) {
                        Map<?, ?> friendMap = (Map<?, ?>) friend;
                        log.debug("检查好友关系数据: {}", friendMap);
                        
                        // 检查发送者或接收者是否是目标用户
                        Object sender = friendMap.get("sender");
                        Object receiver = friendMap.get("receiver");
                        
                        if (sender instanceof Map && receiver instanceof Map) {
                            Map<?, ?> senderMap = (Map<?, ?>) sender;
                            Map<?, ?> receiverMap = (Map<?, ?>) receiver;
                            
                            // 转换为字符串进行比较，避免类型不匹配问题
                            String senderIdStr = String.valueOf(senderMap.get("id"));
                            String receiverIdStr = String.valueOf(receiverMap.get("id"));
                            String userIdStr = String.valueOf(userId);
                            
                            log.debug("比较发送者ID={} 接收者ID={} 与目标ID={}", senderIdStr, receiverIdStr, userIdStr);
                            
                            if (senderIdStr.equals(userIdStr)) {
                                log.info("在好友关系中找到目标用户(发送者): {}", senderMap);
                                return ResponseEntity.ok(senderMap);
                            } else if (receiverIdStr.equals(userIdStr)) {
                                log.info("在好友关系中找到目标用户(接收者): {}", receiverMap);
                                return ResponseEntity.ok(receiverMap);
                            }
                        }
                    }
                }
                log.info("在好友关系中未找到用户ID={}", userId);
            } else {
                log.info("获取好友列表失败或列表为空");
            }
            
            // 如果无法获取用户信息，返回一个默认信息
            Map<String, Object> defaultUser = new HashMap<>();
            defaultUser.put("id", userId);
            defaultUser.put("nickname", "用户" + userId);
            defaultUser.put("username", "user" + userId);
            defaultUser.put("status", "ONLINE"); // 默认为在线状态
            defaultUser.put("avatar", null);
            log.info("返回默认用户信息: {}", defaultUser);
            return ResponseEntity.ok(defaultUser);
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            // 返回一个默认的用户信息，而不是错误状态
            Map<String, Object> defaultUser = new HashMap<>();
            defaultUser.put("id", userId);
            defaultUser.put("nickname", "用户" + userId);
            defaultUser.put("username", "user" + userId);
            defaultUser.put("status", "ONLINE");
            defaultUser.put("avatar", null);
            return ResponseEntity.ok(defaultUser);
        }
    }
    
    @GetMapping("/messages/unread/{userId}")
    public ResponseEntity<?> getUnreadMessages(@PathVariable Long userId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null || !user.getId().equals(userId)) {
            log.error("获取未读消息失败：用户未登录或ID不匹配");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("获取未读消息：用户ID={}", userId);
        try {
            return messageServiceClient.getUnreadMessages(userId);
        } catch (Exception e) {
            log.error("获取未读消息失败: {}", e.getMessage(), e);
            // 返回空列表而不是错误状态
            return ResponseEntity.ok(new ArrayList<>());
        }
    }
    
    @PostMapping("/messages/mark-read")
    public ResponseEntity<?> markMessagesAsRead(@RequestBody Map<String, Long> request, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null) {
            log.error("标记消息已读失败：用户未登录");
            return ResponseEntity.badRequest().build();
        }
        
        Long receiverId = request.get("receiverId");
        Long senderId = request.get("senderId");
        
        if (!user.getId().equals(receiverId)) {
            log.error("标记消息已读失败：接收者ID不匹配");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("标记消息已读：接收者ID={}, 发送者ID={}", receiverId, senderId);
        try {
            return messageServiceClient.markMessagesAsRead(request);
        } catch (Exception e) {
            log.error("标记消息已读失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(0); // 返回0表示没有消息被标记
        }
    }
    
    @PostMapping("/messages/mark-all-read/{userId}")
    public ResponseEntity<?> markAllMessagesAsRead(@PathVariable Long userId, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null || !user.getId().equals(userId)) {
            log.error("标记所有消息已读失败：用户未登录或ID不匹配");
            return ResponseEntity.badRequest().build();
        }
        
        log.info("标记所有消息已读：用户ID={}", userId);
        try {
            return messageServiceClient.markAllMessagesAsRead(userId);
        } catch (Exception e) {
            log.error("标记所有消息已读失败: {}", e.getMessage(), e);
            return ResponseEntity.ok(0); // 返回0表示没有消息被标记
        }
    }
    
    @GetMapping("/debug/session")
    public ResponseEntity<?> debugSession(HttpSession session) {
        Map<String, Object> sessionInfo = new HashMap<>();
        sessionInfo.put("sessionId", session.getId());
        sessionInfo.put("user", session.getAttribute("user"));
        sessionInfo.put("hasJwtToken", session.getAttribute("jwtToken") != null);
        sessionInfo.put("jwtTokenLength", 
                session.getAttribute("jwtToken") != null ? 
                ((String) session.getAttribute("jwtToken")).length() : 0);
        
        log.info("会话调试信息: {}", sessionInfo);
        return ResponseEntity.ok(sessionInfo);
    }

    @PostMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestParam String status, HttpSession session) {
        UserResponseDto user = (UserResponseDto) session.getAttribute("user");
        if (user == null || !user.getId().equals(userId)) {
            log.error("更新用户状态失败：用户未登录或ID不匹配");
            return ResponseEntity.badRequest().build();
        }
        
        // 检查JWT token状态
        String jwtToken = (String) session.getAttribute("jwtToken");
        if (jwtToken == null || jwtToken.trim().isEmpty()) {
            log.error("更新用户状态失败：JWT token为空，用户ID={}, 状态={}", userId, status);
            return ResponseEntity.status(401).body(Map.of("error", "认证令牌缺失"));
        }
        
        log.info("更新用户状态：用户ID={}, 新状态={}, JWT状态={}", userId, status, 
                jwtToken != null ? "已设置" : "未设置");
        try {
            String authHeader = "Bearer " + jwtToken;
            ResponseEntity<UserResponseDto> response = userServiceClient.updateUserStatusWithAuth(userId, status, authHeader);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 更新会话中的用户信息
                UserResponseDto updatedUser = response.getBody();
                session.setAttribute("user", updatedUser);
                log.info("用户状态已更新并同步到会话: {}", updatedUser);
            }
            return response;
        } catch (feign.FeignException.Unauthorized e) {
            log.error("更新用户状态失败 - 未授权: 用户ID={}, 状态={}, JWT状态={}, 错误={}", 
                    userId, status, jwtToken != null ? "已设置" : "未设置", e.getMessage());
            return ResponseEntity.status(401).body(Map.of("error", "认证失败，状态更新失败"));
        } catch (Exception e) {
            log.error("更新用户状态失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "状态更新失败"));
        }
    }
} 