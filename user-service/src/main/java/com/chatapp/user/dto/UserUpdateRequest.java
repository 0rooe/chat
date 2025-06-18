package com.chatapp.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    
    private String nickname;
    private String avatar;
} 