package com.chatapp.relationship.service.impl;

import com.chatapp.relationship.client.UserServiceClient;
import com.chatapp.relationship.dto.GroupDto;
import com.chatapp.relationship.dto.GroupMemberDto;
import com.chatapp.relationship.dto.GroupAnnouncementDto;
import com.chatapp.relationship.exception.NotAuthorizedException;
import com.chatapp.relationship.exception.RelationshipAlreadyExistsException;
import com.chatapp.relationship.exception.ResourceNotFoundException;
import com.chatapp.relationship.model.Group;
import com.chatapp.relationship.model.GroupMember;
import com.chatapp.relationship.model.GroupAnnouncement;
import com.chatapp.relationship.repository.GroupMemberRepository;
import com.chatapp.relationship.repository.GroupRepository;
import com.chatapp.relationship.repository.GroupAnnouncementRepository;
import com.chatapp.relationship.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupAnnouncementRepository groupAnnouncementRepository;
    private final UserServiceClient userServiceClient;

    @Override
    @Transactional
    public GroupDto createGroup(Long userId, GroupDto groupDto) {
        // 创建群组
        Group group = Group.builder()
                .name(groupDto.getName())
                .description(groupDto.getDescription())
                .ownerId(userId)
                .avatar(groupDto.getAvatar())
                .createTime(LocalDateTime.now())
                .maxMembers(groupDto.getMaxMembers() != null ? groupDto.getMaxMembers() : 200)
                .build();

        Group savedGroup = groupRepository.save(group);
        log.info("用户 {} 创建群组: {}", userId, savedGroup.getName());

        // 创建群主成员记录
        GroupMember ownerMember = GroupMember.builder()
                .groupId(savedGroup.getId())
                .userId(userId)
                .role(GroupMember.MemberRole.OWNER)
                .joinTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .build();

        groupMemberRepository.save(ownerMember);

        // 返回带有成员数量的DTO
        GroupDto result = GroupDto.fromEntity(savedGroup);
        result.setMemberCount(1L);  // 创建时只有一个成员（群主）
        return result;
    }

    @Override
    @Transactional
    public GroupDto updateGroup(Long userId, Long groupId, GroupDto groupDto) {
        // 验证操作权限
        GroupMember member = validateGroupPermission(userId, groupId, true);
        
        // 获取群组
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        // 更新群组信息
        if (groupDto.getName() != null) {
            group.setName(groupDto.getName());
        }
        
        if (groupDto.getDescription() != null) {
            group.setDescription(groupDto.getDescription());
        }
        
        if (groupDto.getAvatar() != null) {
            group.setAvatar(groupDto.getAvatar());
        }
        
        if (groupDto.getMaxMembers() != null) {
            group.setMaxMembers(groupDto.getMaxMembers());
        }
        
        group.setUpdateTime(LocalDateTime.now());
        Group updatedGroup = groupRepository.save(group);
        log.info("用户 {} 更新群组: {}", userId, updatedGroup.getName());
        
        // 返回带有成员数量的DTO
        GroupDto result = GroupDto.fromEntity(updatedGroup);
        result.setMemberCount(groupMemberRepository.countByGroupId(groupId));
        return result;
    }

    @Override
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        // 验证是否为群主
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        if (!group.getOwnerId().equals(userId)) {
            throw new NotAuthorizedException("只有群主可以解散群组");
        }
        
        // 先删除所有群成员记录
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        groupMemberRepository.deleteAll(members);
        
        // 再删除群组
        groupRepository.delete(group);
        log.info("用户 {} 解散群组: {}", userId, group.getName());
    }

    @Override
    public GroupDto getGroupById(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        GroupDto groupDto = GroupDto.fromEntity(group);
        groupDto.setMemberCount(groupMemberRepository.countByGroupId(groupId));
        
        // 获取创建者信息
        try {
            Map<String, Object> ownerInfo = userServiceClient.getUserBasicInfo(group.getOwnerId(), null);
            if (ownerInfo != null && ownerInfo.containsKey("nickname")) {
                groupDto.setOwnerName(ownerInfo.get("nickname").toString());
            } else if (ownerInfo != null && ownerInfo.containsKey("username")) {
                groupDto.setOwnerName(ownerInfo.get("username").toString());
            } else {
                groupDto.setOwnerName("用户" + group.getOwnerId());
            }
        } catch (Exception e) {
            log.warn("获取群组创建者信息失败: {}, 群组ID: {}, 创建者ID: {}", e.getMessage(), groupId, group.getOwnerId());
            groupDto.setOwnerName("用户" + group.getOwnerId());
        }
        
        return groupDto;
    }

    @Override
    public List<GroupDto> getGroupsByUserId(Long userId) {
        // 获取用户所在的所有群组
        List<Group> groups = groupRepository.findGroupsByUserId(userId);
        
        return groups.stream()
                .map(group -> {
                    GroupDto dto = GroupDto.fromEntity(group);
                    dto.setMemberCount(groupMemberRepository.countByGroupId(group.getId()));
                    
                    // 获取创建者信息
                    try {
                        Map<String, Object> ownerInfo = userServiceClient.getUserBasicInfo(group.getOwnerId(), userId);
                        if (ownerInfo != null && ownerInfo.containsKey("nickname")) {
                            dto.setOwnerName(ownerInfo.get("nickname").toString());
                        } else if (ownerInfo != null && ownerInfo.containsKey("username")) {
                            dto.setOwnerName(ownerInfo.get("username").toString());
                        } else {
                            dto.setOwnerName("用户" + group.getOwnerId());
                        }
                    } catch (Exception e) {
                        log.warn("获取群组创建者信息失败: {}, 群组ID: {}, 创建者ID: {}", e.getMessage(), group.getId(), group.getOwnerId());
                        dto.setOwnerName("用户" + group.getOwnerId());
                    }
                    
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupDto> searchGroupsByName(String name) {
        // 按名称搜索群组
        List<Group> groups = groupRepository.findGroupsByNameContainingIgnoreCase(name);
        
        return groups.stream()
                .map(group -> {
                    GroupDto dto = GroupDto.fromEntity(group);
                    dto.setMemberCount(groupMemberRepository.countByGroupId(group.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupMemberDto joinGroup(Long userId, Long groupId) {
        // 检查群组是否存在
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        // 检查用户是否已经是群成员
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RelationshipAlreadyExistsException("您已经是该群成员");
        }
        
        // 检查群组是否已满
        long memberCount = groupMemberRepository.countByGroupId(groupId);
        if (memberCount >= group.getMaxMembers()) {
            throw new IllegalStateException("群组已满");
        }
        
        // 创建成员记录
        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMember.MemberRole.MEMBER)
                .joinTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .build();
        
        GroupMember savedMember = groupMemberRepository.save(member);
        log.info("用户 {} 加入群组: {}", userId, group.getName());
        
        return GroupMemberDto.fromEntity(savedMember);
    }

    @Override
    @Transactional
    public GroupMemberDto inviteUserToGroup(Long inviterId, Long userId, Long groupId) {
        // 验证邀请人是否有权限邀请
        validateGroupPermission(inviterId, groupId, false);
        
        // 检查被邀请人是否已经是群成员
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new RelationshipAlreadyExistsException("该用户已经是群成员");
        }
        
        // 检查群组是否已满
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        long memberCount = groupMemberRepository.countByGroupId(groupId);
        if (memberCount >= group.getMaxMembers()) {
            throw new IllegalStateException("群组已满");
        }
        
        // 创建成员记录
        GroupMember member = GroupMember.builder()
                .groupId(groupId)
                .userId(userId)
                .role(GroupMember.MemberRole.MEMBER)
                .joinTime(LocalDateTime.now())
                .lastActiveTime(LocalDateTime.now())
                .build();
        
        GroupMember savedMember = groupMemberRepository.save(member);
        log.info("用户 {} 邀请用户 {} 加入群组: {}", inviterId, userId, group.getName());
        
        return GroupMemberDto.fromEntity(savedMember);
    }

    @Override
    @Transactional
    public void leaveGroup(Long userId, Long groupId) {
        // 验证用户是否在该群组中
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("您不是该群成员"));
        
        // 群主不能退出群组，必须先转让群主或解散群组
        if (member.getRole() == GroupMember.MemberRole.OWNER) {
            throw new IllegalStateException("群主不能退出群组，请先转让群主或解散群组");
        }
        
        // 退出群组
        groupMemberRepository.delete(member);
        log.info("用户 {} 退出群组", userId);
    }

    @Override
    @Transactional
    public void removeGroupMember(Long operatorId, Long userId, Long groupId) {
        // 验证操作权限（群主或管理员）
        GroupMember operatorMember = validateGroupPermission(operatorId, groupId, false);
        
        // 获取要移除的成员
        GroupMember memberToRemove = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("该用户不是群成员"));
        
        // 检查权限等级（不能移除比自己等级高的成员）
        if (memberToRemove.getRole() == GroupMember.MemberRole.OWNER ||
                (memberToRemove.getRole() == GroupMember.MemberRole.ADMIN && operatorMember.getRole() != GroupMember.MemberRole.OWNER)) {
            throw new NotAuthorizedException("您没有权限移除该成员");
        }
        
        // 移除成员
        groupMemberRepository.delete(memberToRemove);
        log.info("操作者 {} 将用户 {} 移出群组", operatorId, userId);
    }

    @Override
    @Transactional
    public GroupMemberDto setGroupAdmin(Long operatorId, Long userId, Long groupId) {
        // 验证是否为群主
        GroupMember operatorMember = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("您不是该群成员"));
        
        if (operatorMember.getRole() != GroupMember.MemberRole.OWNER) {
            throw new NotAuthorizedException("只有群主可以设置管理员");
        }
        
        // 获取要设置为管理员的成员
        GroupMember memberToPromote = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("该用户不是群成员"));
        
        // 设置为管理员
        memberToPromote.setRole(GroupMember.MemberRole.ADMIN);
        GroupMember updatedMember = groupMemberRepository.save(memberToPromote);
        log.info("群主 {} 将用户 {} 设置为管理员", operatorId, userId);
        
        return GroupMemberDto.fromEntity(updatedMember);
    }

    @Override
    @Transactional
    public GroupMemberDto revokeGroupAdmin(Long operatorId, Long userId, Long groupId) {
        // 验证是否为群主
        GroupMember operatorMember = groupMemberRepository.findByGroupIdAndUserId(groupId, operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("您不是该群成员"));
        
        if (operatorMember.getRole() != GroupMember.MemberRole.OWNER) {
            throw new NotAuthorizedException("只有群主可以撤销管理员");
        }
        
        // 获取要撤销管理员的成员
        GroupMember memberToDemote = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("该用户不是群成员"));
        
        if (memberToDemote.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new IllegalStateException("该用户不是管理员");
        }
        
        // 撤销管理员
        memberToDemote.setRole(GroupMember.MemberRole.MEMBER);
        GroupMember updatedMember = groupMemberRepository.save(memberToDemote);
        log.info("群主 {} 撤销用户 {} 的管理员权限", operatorId, userId);
        
        return GroupMemberDto.fromEntity(updatedMember);
    }

    @Override
    public List<GroupMemberDto> getGroupMembers(Long groupId) {
        // 检查群组是否存在
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("群组不存在");
        }
        
        // 获取所有群成员
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        
        return members.stream()
                .map(GroupMemberDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupMemberDto> getGroupAdmins(Long groupId) {
        // 检查群组是否存在
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("群组不存在");
        }
        
        // 获取群主和管理员
        List<GroupMember> admins = groupMemberRepository.findByGroupIdAndRole(groupId, GroupMember.MemberRole.ADMIN);
        List<GroupMember> owners = groupMemberRepository.findByGroupIdAndRole(groupId, GroupMember.MemberRole.OWNER);
        
        // 合并结果
        admins.addAll(owners);
        
        return admins.stream()
                .map(GroupMemberDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GroupMemberDto muteGroupMember(Long operatorId, Long userId, Long groupId) {
        // 验证操作权限
        validateGroupPermission(operatorId, groupId, false);
        
        // 获取要禁言的成员
        GroupMember memberToMute = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("该用户不是群成员"));
        
        // 不能禁言群主或管理员
        if (memberToMute.getRole() == GroupMember.MemberRole.OWNER || memberToMute.getRole() == GroupMember.MemberRole.ADMIN) {
            throw new NotAuthorizedException("无法禁言群主或管理员");
        }
        
        // 设置禁言
        memberToMute.setMuted(true);
        GroupMember updatedMember = groupMemberRepository.save(memberToMute);
        log.info("操作者 {} 禁言用户 {}", operatorId, userId);
        
        return GroupMemberDto.fromEntity(updatedMember);
    }

    @Override
    @Transactional
    public GroupMemberDto unmuteGroupMember(Long operatorId, Long userId, Long groupId) {
        // 验证操作权限
        validateGroupPermission(operatorId, groupId, false);
        
        // 获取要解除禁言的成员
        GroupMember memberToUnmute = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("该用户不是群成员"));
        
        // 设置解除禁言
        memberToUnmute.setMuted(false);
        GroupMember updatedMember = groupMemberRepository.save(memberToUnmute);
        log.info("操作者 {} 解除用户 {} 的禁言", operatorId, userId);
        
        return GroupMemberDto.fromEntity(updatedMember);
    }
    
    /**
     * 验证操作权限
     * @param userId 用户ID
     * @param groupId 群组ID
     * @param ownerOnly 是否只允许群主操作
     * @return 成员信息
     */
    private GroupMember validateGroupPermission(Long userId, Long groupId, boolean ownerOnly) {
        // 检查群组是否存在
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        // 获取用户在群组中的身份
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotAuthorizedException("您不是该群成员"));
        
        // 检查权限
        if (ownerOnly && !group.getOwnerId().equals(userId)) {
            throw new NotAuthorizedException("只有群主有权限执行此操作");
        } else if (!ownerOnly && member.getRole() == GroupMember.MemberRole.MEMBER) {
            throw new NotAuthorizedException("只有群主或管理员有权限执行此操作");
        }
        
        return member;
    }
    
    @Override
    @Transactional
    public GroupAnnouncementDto publishGroupAnnouncement(Long userId, Long groupId, GroupAnnouncementDto announcementDto) {
        // 验证群组存在
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("群组不存在"));
        
        // 验证用户权限（群主或管理员）
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new NotAuthorizedException("您不是该群组成员"));
        
        if (member.getRole() != GroupMember.MemberRole.OWNER && member.getRole() != GroupMember.MemberRole.ADMIN) {
            throw new NotAuthorizedException("只有群主或管理员可以发布公告");
        }
        
        // 获取发布者用户信息
        String publisherName = "用户" + userId;
        try {
            Map<String, Object> userInfo = userServiceClient.getUserBasicInfo(userId, userId);
            if (userInfo != null && userInfo.containsKey("nickname")) {
                publisherName = userInfo.get("nickname").toString();
            } else if (userInfo != null && userInfo.containsKey("username")) {
                publisherName = userInfo.get("username").toString();
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }
        
        // 创建群公告
        GroupAnnouncement announcement = GroupAnnouncement.builder()
                .groupId(groupId)
                .title(announcementDto.getTitle())
                .content(announcementDto.getContent())
                .publisherId(userId)
                .publisherName(publisherName)
                .publishTime(LocalDateTime.now())
                .isActive(true)
                .build();
        
        GroupAnnouncement savedAnnouncement = groupAnnouncementRepository.save(announcement);
        log.info("用户 {} 在群组 {} 发布公告: {}", userId, groupId, savedAnnouncement.getTitle());
        
        return GroupAnnouncementDto.fromEntity(savedAnnouncement);
    }
    
    @Override
    public GroupAnnouncementDto getLatestGroupAnnouncement(Long groupId) {
        Optional<GroupAnnouncement> announcement = groupAnnouncementRepository
                .findFirstByGroupIdAndIsActiveTrueOrderByPublishTimeDesc(groupId);
        
        return announcement.map(GroupAnnouncementDto::fromEntity).orElse(null);
    }
    
    @Override
    public List<GroupAnnouncementDto> getGroupAnnouncements(Long groupId) {
        List<GroupAnnouncement> announcements = groupAnnouncementRepository
                .findByGroupIdAndIsActiveTrueOrderByPublishTimeDesc(groupId);
        
        return announcements.stream()
                .map(GroupAnnouncementDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteGroupAnnouncement(Long userId, Long announcementId) {
        GroupAnnouncement announcement = groupAnnouncementRepository.findById(announcementId)
                .orElseThrow(() -> new ResourceNotFoundException("公告不存在"));
        
        // 验证用户权限（只有发布者、群主或管理员可以删除）
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(announcement.getGroupId(), userId)
                .orElseThrow(() -> new NotAuthorizedException("您不是该群组成员"));
        
        boolean canDelete = announcement.getPublisherId().equals(userId) ||
                           member.getRole() == GroupMember.MemberRole.OWNER ||
                           member.getRole() == GroupMember.MemberRole.ADMIN;
        
        if (!canDelete) {
            throw new NotAuthorizedException("您没有权限删除此公告");
        }
        
        // 软删除：标记为不活跃
        announcement.setIsActive(false);
        groupAnnouncementRepository.save(announcement);
        log.info("用户 {} 删除公告 {}", userId, announcementId);
    }
} 