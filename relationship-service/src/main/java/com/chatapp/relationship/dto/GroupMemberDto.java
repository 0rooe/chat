package com.chatapp.relationship.dto;

import com.chatapp.relationship.model.GroupMember;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberDto {
    private Long id;
    private Long groupId;
    private Long userId;
    private String role;
    private LocalDateTime joinTime;
    private LocalDateTime lastActiveTime;
    private boolean muted;
    
    // 可能包括用户信息的扩展字段
    private String username;
    private String nickname;
    private String avatar;
    
    public static GroupMemberDto fromEntity(GroupMember member) {
        return GroupMemberDto.builder()
                .id(member.getId())
                .groupId(member.getGroupId())
                .userId(member.getUserId())
                .role(member.getRole().name())
                .joinTime(member.getJoinTime())
                .lastActiveTime(member.getLastActiveTime())
                .muted(member.isMuted())
                .build();
    }
} 