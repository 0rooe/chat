package com.chatapp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AuthRequest {
    
    @NotBlank
    private String username;
    
    @NotBlank
    private String password;
} 