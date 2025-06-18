package com.chatapp.relationship.service.impl;

import com.chatapp.relationship.client.UserServiceClient;
import com.chatapp.relationship.dto.FriendRequestDto;
import com.chatapp.relationship.dto.FriendshipDto;
import com.chatapp.relationship.exception.RelationshipAlreadyExistsException;
import com.chatapp.relationship.exception.ResourceNotFoundException;
import com.chatapp.relationship.model.Friendship;
import com.chatapp.relationship.repository.FriendshipRepository;
import com.chatapp.relationship.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendshipServiceImpl implements FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserServiceClient userServiceClient;
    private final WebClient.Builder webClientBuilder;

    @Override
    @Transactional
    public FriendshipDto sendFriendRequest(Long userId, FriendRequestDto friendRequestDto) {
        Long friendId = friendRequestDto.getFriendId();
        
        // 验证不能向自己发送好友请求
        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("不能向自己发送好友请求");
        }
        
        // 检查是否已经是好友
        Optional<Friendship> existingFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (existingFriendship.isPresent() && existingFriendship.get().getStatus() == Friendship.FriendshipStatus.ACCEPTED) {
            throw new RelationshipAlreadyExistsException("已经是好友关系");
        }
        
        // 检查是否已经发送过请求
        if (existingFriendship.isPresent() && existingFriendship.get().getStatus() == Friendship.FriendshipStatus.PENDING) {
            throw new RelationshipAlreadyExistsException("好友请求已发送");
        }
        
        // 检查是否对方也发送了好友请求（双向请求）
        Optional<Friendship> reverseRequest = friendshipRepository.findByUserIdAndFriendId(friendId, userId);

        if (reverseRequest.isPresent() && reverseRequest.get().getStatus() == Friendship.FriendshipStatus.PENDING) {
            // 如果对方已经发送了好友请求，则自动接受
            Friendship friendship = reverseRequest.get();
            friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
            friendship.setUpdateTime(LocalDateTime.now());
            Friendship updatedFriendship = friendshipRepository.save(friendship);
            log.info("自动接受好友请求: {} -> {}", userId, friendId);
            
            // 发送好友接受通知
            sendFriendAcceptNotification(friendship.getUserId(), userId);
            
            return FriendshipDto.fromEntity(updatedFriendship);
        }
        
        // 创建新的好友请求
        Friendship friendship = Friendship.builder()
                .userId(userId)
                .friendId(friendId)
                .status(Friendship.FriendshipStatus.PENDING)
                .createTime(LocalDateTime.now())
                .build();
        
        Friendship savedFriendship = friendshipRepository.save(friendship);
        log.info("发送好友请求: {} -> {}", userId, friendId);
        
        // 发送好友请求通知
        sendFriendRequestNotification(userId, friendId);
        
        return FriendshipDto.fromEntity(savedFriendship);
    }

    @Override
    @Transactional
    public FriendshipDto acceptFriendRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("好友请求不存在"));
        
        // 验证请求是否是发给当前用户的
        if (!friendship.getFriendId().equals(userId)) {
            throw new ResourceNotFoundException("好友请求不存在或不属于当前用户");
        }
        
        // 验证状态是否为待处理
        if (friendship.getStatus() != Friendship.FriendshipStatus.PENDING) {
            throw new IllegalStateException("好友请求状态不正确");
        }
        
        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendship.setUpdateTime(LocalDateTime.now());
        Friendship updatedFriendship = friendshipRepository.save(friendship);
        log.info("接受好友请求: {} -> {}", friendship.getUserId(), userId);
        
        // 发送好友接受通知
        sendFriendAcceptNotification(friendship.getUserId(), userId);
        
        return FriendshipDto.fromEntity(updatedFriendship);
    }

    // 发送好友请求通知
    private void sendFriendRequestNotification(Long senderId, Long receiverId) {
        try {
            // 获取发送者信息
            Map<String, Object> senderInfo = userServiceClient.getUserBasicInfo(senderId, senderId);
            String senderName = (String) senderInfo.getOrDefault("nickname", "未知用户");
            
            // 构建通知数据
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", receiverId);
            notificationData.put("type", "FRIEND_REQUEST");
            notificationData.put("title", "新的好友请求");
            notificationData.put("content", senderName + " 向您发送了好友请求");
            notificationData.put("referenceId", String.valueOf(senderId));
            
            // 发送通知到通知服务
            webClientBuilder.build()
                    .post()
                    .uri("http://notification-service/api/v1/notifications/send")
                    .bodyValue(notificationData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("好友请求通知发送成功: {}", response),
                            error -> log.error("发送好友请求通知失败: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.error("发送好友请求通知失败: {}", e.getMessage(), e);
        }
    }
    
    // 发送好友接受通知
    private void sendFriendAcceptNotification(Long receiverId, Long accepterId) {
        try {
            // 获取接受者信息
            Map<String, Object> accepterInfo = userServiceClient.getUserBasicInfo(accepterId, accepterId);
            String accepterName = (String) accepterInfo.getOrDefault("nickname", "未知用户");
            
            // 构建通知数据
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("userId", receiverId);
            notificationData.put("type", "FRIEND_ACCEPT");
            notificationData.put("title", "好友请求已接受");
            notificationData.put("content", accepterName + " 接受了您的好友请求");
            notificationData.put("referenceId", String.valueOf(accepterId));
            
            // 发送通知到通知服务
            webClientBuilder.build()
                    .post()
                    .uri("http://notification-service/api/v1/notifications/send")
                    .bodyValue(notificationData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .subscribe(
                            response -> log.info("好友接受通知发送成功: {}", response),
                            error -> log.error("发送好友接受通知失败: {}", error.getMessage())
                    );
        } catch (Exception e) {
            log.error("发送好友接受通知失败: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void rejectFriendRequest(Long userId, Long requestId) {
        Friendship friendship = friendshipRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("好友请求不存在"));
        
        // 验证请求是否是发给当前用户的
        if (!friendship.getFriendId().equals(userId)) {
            throw new ResourceNotFoundException("好友请求不存在或不属于当前用户");
        }
        
        // 直接删除该请求
        friendshipRepository.delete(friendship);
        log.info("拒绝好友请求: {} -> {}", friendship.getUserId(), userId);
    }

    @Override
    @Transactional
    public FriendshipDto blockFriend(Long userId, Long friendId) {
        Optional<Friendship> existingFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        
        Friendship friendship;
        if (existingFriendship.isPresent()) {
            friendship = existingFriendship.get();
            friendship.setStatus(Friendship.FriendshipStatus.BLOCKED);
            friendship.setUpdateTime(LocalDateTime.now());
        } else {
            friendship = Friendship.builder()
                    .userId(userId)
                    .friendId(friendId)
                    .status(Friendship.FriendshipStatus.BLOCKED)
                    .createTime(LocalDateTime.now())
                    .build();
        }
        
        Friendship savedFriendship = friendshipRepository.save(friendship);
        log.info("拉黑好友: {} -> {}", userId, friendId);
        return FriendshipDto.fromEntity(savedFriendship);
    }

    @Override
    @Transactional
    public FriendshipDto unblockFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到相应的好友关系"));
        
        if (friendship.getStatus() != Friendship.FriendshipStatus.BLOCKED) {
            throw new IllegalStateException("该用户未被拉黑");
        }
        
        friendship.setStatus(Friendship.FriendshipStatus.ACCEPTED);
        friendship.setUpdateTime(LocalDateTime.now());
        Friendship updatedFriendship = friendshipRepository.save(friendship);
        log.info("解除拉黑: {} -> {}", userId, friendId);
        return FriendshipDto.fromEntity(updatedFriendship);
    }

    @Override
    @Transactional
    public void deleteFriend(Long userId, Long friendId) {
        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到相应的好友关系"));
        
        // 删除好友关系
        friendshipRepository.delete(friendship);
        log.info("删除好友: {} -> {}", userId, friendId);
    }
    
    @Override
    @Transactional
    public void deleteFriendshipById(Long userId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到相应的好友关系"));
        
        // 验证用户是否有权限删除此关系（只能删除自己的好友关系）
        if (!friendship.getUserId().equals(userId) && !friendship.getFriendId().equals(userId)) {
            throw new ResourceNotFoundException("未找到相应的好友关系或无权限删除");
        }
        
        // 删除好友关系
        friendshipRepository.delete(friendship);
        log.info("通过关系ID删除好友: 用户ID={}, 关系ID={}", userId, friendshipId);
    }

    @Override
    public List<FriendshipDto> getFriends(Long userId) {
        List<Friendship> friends = friendshipRepository.findAllByUserId(userId).stream()
                .filter(f -> f.getStatus() == Friendship.FriendshipStatus.ACCEPTED)
                .collect(Collectors.toList());
        
        List<FriendshipDto> result = new ArrayList<>();
        
        for (Friendship friendship : friends) {
            FriendshipDto dto = FriendshipDto.fromEntity(friendship);
            
            try {
                // 确定好友ID（可能是userId或friendId）
                Long friendId = friendship.getUserId().equals(userId) ? friendship.getFriendId() : friendship.getUserId();
                
                // 获取发送者信息（当前用户）
                log.debug("正在获取用户(ID={})信息", userId);
                Map<String, Object> currentUserInfo = userServiceClient.getUserBasicInfo(userId, userId);
                
                // 获取好友信息
                log.debug("正在获取好友(ID={})信息", friendId);
                Map<String, Object> friendInfo = userServiceClient.getUserBasicInfo(friendId, userId);
                
                // 设置发送者和接收者（好友关系中没有真正的发送者/接收者，但我们需要提供数据给前端）
                dto.setSender(currentUserInfo);
                dto.setReceiver(friendInfo);
            } catch (Exception e) {
                log.error("获取用户信息失败: {}", e.getMessage(), e);
                // 确保DTO中包含用户ID信息
                Long friendId = friendship.getUserId().equals(userId) ? friendship.getFriendId() : friendship.getUserId();
                
                Map<String, Object> currentUserInfo = new HashMap<>();
                currentUserInfo.put("id", userId);
                currentUserInfo.put("nickname", "未知用户");
                currentUserInfo.put("username", "unknown");
                currentUserInfo.put("status", "OFFLINE");
                
                Map<String, Object> friendInfo = new HashMap<>();
                friendInfo.put("id", friendId);
                friendInfo.put("nickname", "未知用户");
                friendInfo.put("username", "unknown");
                friendInfo.put("status", "OFFLINE");
                
                dto.setSender(currentUserInfo);
                dto.setReceiver(friendInfo);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<FriendshipDto> getPendingRequests(Long userId) {
        // 获取发给当前用户的待处理请求
        List<Friendship> pendingRequests = friendshipRepository.findByFriendIdAndStatus(userId, Friendship.FriendshipStatus.PENDING);
        
        List<FriendshipDto> result = new ArrayList<>();
        
        for (Friendship request : pendingRequests) {
            FriendshipDto dto = FriendshipDto.fromEntity(request);
            
            try {
                // 获取发送者信息
                log.debug("正在获取发送者(ID={})信息", request.getUserId());
                Map<String, Object> sender = userServiceClient.getUserBasicInfo(request.getUserId(), userId);
                dto.setSender(sender);
                
                // 获取接收者信息（当前用户）
                log.debug("正在获取接收者(ID={})信息", userId);
                Map<String, Object> receiver = userServiceClient.getUserBasicInfo(userId, userId);
                dto.setReceiver(receiver);
            } catch (Exception e) {
                log.error("获取用户信息失败: {}", e.getMessage(), e);
                // 确保DTO中包含用户ID信息
                Map<String, Object> emptySender = new HashMap<>();
                emptySender.put("id", request.getUserId());
                emptySender.put("nickname", "未知用户");
                emptySender.put("username", "unknown");
                emptySender.put("status", "OFFLINE");
                dto.setSender(emptySender);
                
                Map<String, Object> emptyReceiver = new HashMap<>();
                emptyReceiver.put("id", userId);
                emptyReceiver.put("nickname", "未知用户");
                emptyReceiver.put("username", "unknown");
                emptyReceiver.put("status", "OFFLINE");
                dto.setReceiver(emptyReceiver);
            }
            
            result.add(dto);
        }
        
        return result;
    }

    @Override
    public List<FriendshipDto> getSentRequests(Long userId) {
        // 获取当前用户发出的待处理请求
        List<Friendship> sentRequests = friendshipRepository.findByUserIdAndStatus(userId, Friendship.FriendshipStatus.PENDING);
        log.info("【调试】获取到的发送请求数: {}", sentRequests.size());
        
        List<FriendshipDto> result = new ArrayList<>();
        
        for (Friendship request : sentRequests) {
            FriendshipDto dto = FriendshipDto.fromEntity(request);
            log.info("【调试】处理发送请求: userId={}, friendId={}, status={}", 
                    request.getUserId(), request.getFriendId(), request.getStatus());
            
            try {
                // 获取发送者信息（当前用户）
                log.info("【调试】正在获取发送者(ID={})信息", userId);
                Map<String, Object> sender = userServiceClient.getUserBasicInfo(userId, userId);
                log.info("【调试】获取到发送者信息: {}", sender);
                dto.setSender(sender);
                
                // 获取接收者信息
                log.info("【调试】正在获取接收者(ID={})信息", request.getFriendId());
                Map<String, Object> receiver = userServiceClient.getUserBasicInfo(request.getFriendId(), userId);
                log.info("【调试】获取到接收者信息: {}", receiver);
                dto.setReceiver(receiver);
            } catch (Exception e) {
                log.error("【错误】获取用户信息失败: {}, 详细异常: ", e.getMessage(), e);
                // 确保DTO中包含用户ID信息
                Map<String, Object> emptySender = new HashMap<>();
                emptySender.put("id", userId);
                emptySender.put("nickname", "未知用户");
                emptySender.put("username", "unknown");
                emptySender.put("status", "OFFLINE");
                dto.setSender(emptySender);
                
                Map<String, Object> emptyReceiver = new HashMap<>();
                emptyReceiver.put("id", request.getFriendId());
                emptyReceiver.put("nickname", "未知用户");
                emptyReceiver.put("username", "unknown");
                emptyReceiver.put("status", "OFFLINE");
                dto.setReceiver(emptyReceiver);
            }
            
            result.add(dto);
            log.info("【调试】添加到结果列表的DTO: {}", dto);
        }
        
        log.info("【调试】返回的结果数量: {}", result.size());
        return result;
    }

    @Override
    public List<FriendshipDto> getBlockedUsers(Long userId) {
        List<Friendship> blockedUsers = friendshipRepository.findByUserIdAndStatus(userId, Friendship.FriendshipStatus.BLOCKED);
        
        return blockedUsers.stream()
                .map(FriendshipDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Friendship.FriendshipStatus getFriendshipStatus(Long userId, Long friendId) {
        // 先检查从userId到friendId的直接关系
        Optional<Friendship> directFriendship = friendshipRepository.findByUserIdAndFriendId(userId, friendId);
        if (directFriendship.isPresent()) {
            return directFriendship.get().getStatus();
        }
        
        // 如果没有直接关系，检查反向关系
        Optional<Friendship> reverseFriendship = friendshipRepository.findByUserIdAndFriendId(friendId, userId);
        if (reverseFriendship.isPresent()) {
            Friendship.FriendshipStatus reverseStatus = reverseFriendship.get().getStatus();
            // 如果反向关系是ACCEPTED，那么关系状态也是ACCEPTED
            if (reverseStatus == Friendship.FriendshipStatus.ACCEPTED) {
                return Friendship.FriendshipStatus.ACCEPTED;
            }
            // 如果反向关系是PENDING，那么双方有待处理的好友关系
            else if (reverseStatus == Friendship.FriendshipStatus.PENDING) {
                return Friendship.FriendshipStatus.PENDING;
            }
        }
        
        return null; // 没有任何关系
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        return friendshipRepository.existsByUserIdAndFriendIdAndStatus(userId, friendId, Friendship.FriendshipStatus.ACCEPTED)
                || friendshipRepository.existsByUserIdAndFriendIdAndStatus(friendId, userId, Friendship.FriendshipStatus.ACCEPTED);
    }

    @Override
    public boolean isRequestSender(Long userId, Long friendId) {
        // 检查用户是否向好友发送了好友请求（尚未接受）
        return friendshipRepository.existsByUserIdAndFriendIdAndStatus(userId, friendId, Friendship.FriendshipStatus.PENDING);
    }

    @Override
    public Map<String, Object> testGetUserInfo(Long userId) {
        log.info("测试获取用户 {} 的信息", userId);
        try {
            Map<String, Object> userInfo = userServiceClient.getUserBasicInfo(userId, userId);
            log.info("成功获取用户信息: {}", userInfo);
            return userInfo;
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            throw e;
        }
    }
} 