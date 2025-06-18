package com.chatapp.user.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class StatusUpdateRequest {
    
    @NotBlank
    private String status;
} 