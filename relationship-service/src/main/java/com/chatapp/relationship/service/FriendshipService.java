package com.chatapp.relationship.service;

import com.chatapp.relationship.dto.FriendRequestDto;
import com.chatapp.relationship.dto.FriendshipDto;
import com.chatapp.relationship.model.Friendship;

import java.util.List;
import java.util.Map;

public interface FriendshipService {
    /**
     * 发送好友请求
     */
    FriendshipDto sendFriendRequest(Long userId, FriendRequestDto friendRequestDto);
    
    /**
     * 接受好友请求
     */
    FriendshipDto acceptFriendRequest(Long userId, Long requestId);
    
    /**
     * 拒绝好友请求
     */
    void rejectFriendRequest(Long userId, Long requestId);
    
    /**
     * 拉黑好友
     */
    FriendshipDto blockFriend(Long userId, Long friendId);
    
    /**
     * 解除拉黑
     */
    FriendshipDto unblockFriend(Long userId, Long friendId);
    
    /**
     * 删除好友
     */
    void deleteFriend(Long userId, Long friendId);
    
    /**
     * 通过关系ID删除好友
     */
    void deleteFriendshipById(Long userId, Long friendshipId);
    
    /**
     * 获取用户的所有好友
     */
    List<FriendshipDto> getFriends(Long userId);
    
    /**
     * 获取收到的待处理的好友请求
     */
    List<FriendshipDto> getPendingRequests(Long userId);
    
    /**
     * 获取发送的待处理的好友请求
     */
    List<FriendshipDto> getSentRequests(Long userId);
    
    /**
     * 获取已拉黑的好友
     */
    List<FriendshipDto> getBlockedUsers(Long userId);
    
    /**
     * 获取好友状态
     */
    Friendship.FriendshipStatus getFriendshipStatus(Long userId, Long friendId);
    
    /**
     * 检查是否已经是好友
     */
    boolean isFriend(Long userId, Long friendId);
    
    /**
     * 检查用户是否为好友请求的发送者
     */
    boolean isRequestSender(Long userId, Long friendId);
    
    /**
     * 测试获取用户信息
     */
    Map<String, Object> testGetUserInfo(Long userId);
} 