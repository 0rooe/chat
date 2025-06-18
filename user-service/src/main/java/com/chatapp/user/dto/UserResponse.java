package com.chatapp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String status;
    private LocalDateTime lastLoginTime;
} 