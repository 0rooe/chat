package com.chatapp.frontend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime lastLoginTime;
} 