package com.chatapp.relationship.controller;

import com.chatapp.relationship.dto.GroupDto;
import com.chatapp.relationship.dto.GroupMemberDto;
import com.chatapp.relationship.dto.GroupAnnouncementDto;
import com.chatapp.relationship.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDto> createGroup(
            @RequestHeader("X-User-ID") Long userId,
            @Valid @RequestBody GroupDto groupDto) {
        log.info("用户 {} 创建群组: {}", userId, groupDto.getName());
        GroupDto createdGroup = groupService.createGroup(userId, groupDto);
        return new ResponseEntity<>(createdGroup, HttpStatus.CREATED);
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupDto> updateGroup(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long groupId,
            @RequestBody GroupDto groupDto) {
        log.info("用户 {} 更新群组 {}: {}", userId, groupId, groupDto.getName());
        GroupDto updatedGroup = groupService.updateGroup(userId, groupId, groupDto);
        return ResponseEntity.ok(updatedGroup);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long groupId) {
        log.info("用户 {} 解散群组 {}", userId, groupId);
        groupService.deleteGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDto> getGroupById(@PathVariable Long groupId) {
        log.info("获取群组详情: {}", groupId);
        GroupDto group = groupService.getGroupById(groupId);
        return ResponseEntity.ok(group);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<GroupDto>> getGroupsByUserId(@PathVariable Long userId) {
        log.info("获取用户 {} 的群组列表", userId);
        List<GroupDto> groups = groupService.getGroupsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/search")
    public ResponseEntity<List<GroupDto>> searchGroupsByName(@RequestParam String name) {
        log.info("搜索群组: {}", name);
        List<GroupDto> groups = groupService.searchGroupsByName(name);
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<GroupMemberDto> joinGroup(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long groupId) {
        log.info("用户 {} 加入群组 {}", userId, groupId);
        GroupMemberDto member = groupService.joinGroup(userId, groupId);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @PostMapping("/{groupId}/invite/{userId}")
    public ResponseEntity<GroupMemberDto> inviteUserToGroup(
            @RequestHeader("X-User-ID") Long inviterId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("用户 {} 邀请用户 {} 加入群组 {}", inviterId, userId, groupId);
        GroupMemberDto member = groupService.inviteUserToGroup(inviterId, userId, groupId);
        return new ResponseEntity<>(member, HttpStatus.CREATED);
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long groupId) {
        log.info("用户 {} 退出群组 {}", userId, groupId);
        groupService.leaveGroup(userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> removeGroupMember(
            @RequestHeader("X-User-ID") Long operatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("操作者 {} 将用户 {} 移出群组 {}", operatorId, userId, groupId);
        groupService.removeGroupMember(operatorId, userId, groupId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{groupId}/members/{userId}/admin")
    public ResponseEntity<GroupMemberDto> setGroupAdmin(
            @RequestHeader("X-User-ID") Long operatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("操作者 {} 将用户 {} 设置为群组 {} 的管理员", operatorId, userId, groupId);
        GroupMemberDto member = groupService.setGroupAdmin(operatorId, userId, groupId);
        return ResponseEntity.ok(member);
    }

    @DeleteMapping("/{groupId}/members/{userId}/admin")
    public ResponseEntity<GroupMemberDto> revokeGroupAdmin(
            @RequestHeader("X-User-ID") Long operatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("操作者 {} 撤销用户 {} 在群组 {} 的管理员权限", operatorId, userId, groupId);
        GroupMemberDto member = groupService.revokeGroupAdmin(operatorId, userId, groupId);
        return ResponseEntity.ok(member);
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMemberDto>> getGroupMembers(@PathVariable Long groupId) {
        log.info("获取群组 {} 的成员列表", groupId);
        List<GroupMemberDto> members = groupService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{groupId}/admins")
    public ResponseEntity<List<GroupMemberDto>> getGroupAdmins(@PathVariable Long groupId) {
        log.info("获取群组 {} 的管理员列表", groupId);
        List<GroupMemberDto> admins = groupService.getGroupAdmins(groupId);
        return ResponseEntity.ok(admins);
    }

    @PutMapping("/{groupId}/members/{userId}/mute")
    public ResponseEntity<GroupMemberDto> muteGroupMember(
            @RequestHeader("X-User-ID") Long operatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("操作者 {} 禁言群组 {} 的成员 {}", operatorId, groupId, userId);
        GroupMemberDto member = groupService.muteGroupMember(operatorId, userId, groupId);
        return ResponseEntity.ok(member);
    }

    @PutMapping("/{groupId}/members/{userId}/unmute")
    public ResponseEntity<GroupMemberDto> unmuteGroupMember(
            @RequestHeader("X-User-ID") Long operatorId,
            @PathVariable Long groupId,
            @PathVariable Long userId) {
        log.info("操作者 {} 解除群组 {} 成员 {} 的禁言", operatorId, groupId, userId);
        GroupMemberDto member = groupService.unmuteGroupMember(operatorId, userId, groupId);
        return ResponseEntity.ok(member);
    }
    
    // ===== 群公告相关API =====
    
    @PostMapping("/{groupId}/announcement")
    public ResponseEntity<GroupAnnouncementDto> publishGroupAnnouncement(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long groupId,
            @Valid @RequestBody GroupAnnouncementDto announcementDto) {
        log.info("用户 {} 在群组 {} 发布公告: {}", userId, groupId, announcementDto.getTitle());
        GroupAnnouncementDto announcement = groupService.publishGroupAnnouncement(userId, groupId, announcementDto);
        return new ResponseEntity<>(announcement, HttpStatus.CREATED);
    }
    
    @GetMapping("/{groupId}/announcement/latest")
    public ResponseEntity<GroupAnnouncementDto> getLatestGroupAnnouncement(@PathVariable Long groupId) {
        log.info("获取群组 {} 的最新公告", groupId);
        GroupAnnouncementDto announcement = groupService.getLatestGroupAnnouncement(groupId);
        if (announcement != null) {
            return ResponseEntity.ok(announcement);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    @GetMapping("/{groupId}/announcements")
    public ResponseEntity<List<GroupAnnouncementDto>> getGroupAnnouncements(@PathVariable Long groupId) {
        log.info("获取群组 {} 的所有公告", groupId);
        List<GroupAnnouncementDto> announcements = groupService.getGroupAnnouncements(groupId);
        return ResponseEntity.ok(announcements);
    }
    
    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteGroupAnnouncement(
            @RequestHeader("X-User-ID") Long userId,
            @PathVariable Long announcementId) {
        log.info("用户 {} 删除群公告 {}", userId, announcementId);
        groupService.deleteGroupAnnouncement(userId, announcementId);
        return ResponseEntity.noContent().build();
    }
} 