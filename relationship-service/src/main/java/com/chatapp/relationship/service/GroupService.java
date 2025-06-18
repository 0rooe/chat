package com.chatapp.relationship.service;

import com.chatapp.relationship.dto.GroupDto;
import com.chatapp.relationship.dto.GroupMemberDto;
import com.chatapp.relationship.dto.GroupAnnouncementDto;

import java.util.List;

public interface GroupService {
    /**
     * 创建群组
     */
    GroupDto createGroup(Long userId, GroupDto groupDto);
    
    /**
     * 更新群组信息
     */
    GroupDto updateGroup(Long userId, Long groupId, GroupDto groupDto);
    
    /**
     * 解散群组
     */
    void deleteGroup(Long userId, Long groupId);
    
    /**
     * 获取群组详情
     */
    GroupDto getGroupById(Long groupId);
    
    /**
     * 获取用户所有群组
     */
    List<GroupDto> getGroupsByUserId(Long userId);
    
    /**
     * 按名称搜索群组
     */
    List<GroupDto> searchGroupsByName(String name);
    
    /**
     * 加入群组
     */
    GroupMemberDto joinGroup(Long userId, Long groupId);
    
    /**
     * 邀请用户加入群组
     */
    GroupMemberDto inviteUserToGroup(Long inviterId, Long userId, Long groupId);
    
    /**
     * 退出群组
     */
    void leaveGroup(Long userId, Long groupId);
    
    /**
     * 移除群成员
     */
    void removeGroupMember(Long operatorId, Long userId, Long groupId);
    
    /**
     * 设置群管理员
     */
    GroupMemberDto setGroupAdmin(Long operatorId, Long userId, Long groupId);
    
    /**
     * 撤销群管理员
     */
    GroupMemberDto revokeGroupAdmin(Long operatorId, Long userId, Long groupId);
    
    /**
     * 获取群成员
     */
    List<GroupMemberDto> getGroupMembers(Long groupId);
    
    /**
     * 获取群管理员
     */
    List<GroupMemberDto> getGroupAdmins(Long groupId);
    
    /**
     * 禁言群成员
     */
    GroupMemberDto muteGroupMember(Long operatorId, Long userId, Long groupId);
    
    /**
     * 解除禁言
     */
    GroupMemberDto unmuteGroupMember(Long operatorId, Long userId, Long groupId);
    
    /**
     * 发布群公告
     */
    GroupAnnouncementDto publishGroupAnnouncement(Long userId, Long groupId, GroupAnnouncementDto announcementDto);
    
    /**
     * 获取群组最新公告
     */
    GroupAnnouncementDto getLatestGroupAnnouncement(Long groupId);
    
    /**
     * 获取群组所有公告
     */
    List<GroupAnnouncementDto> getGroupAnnouncements(Long groupId);
    
    /**
     * 删除群公告
     */
    void deleteGroupAnnouncement(Long userId, Long announcementId);
} 