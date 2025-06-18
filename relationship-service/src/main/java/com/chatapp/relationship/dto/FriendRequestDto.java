package com.chatapp.relationship.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestDto {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;
} 