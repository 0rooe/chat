package com.chatapp.user.dto;

import com.chatapp.user.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private User.UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
    
    // 与当前用户的关系状态（NONE, PENDING_SENT, PENDING_RECEIVED, ACCEPTED, BLOCKED）
    private String relationshipStatus;
    
    public static UserResponseDto fromUser(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }
} 