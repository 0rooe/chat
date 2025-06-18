package com.chatapp.relationship.dto;

import com.chatapp.relationship.model.Friendship;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FriendshipDto {
    private Long id;
    private Long userId;
    private Long friendId;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 用户相关信息
    private Map<String, Object> sender;
    private Map<String, Object> receiver;
    
    public static FriendshipDto fromEntity(Friendship friendship) {
        return FriendshipDto.builder()
                .id(friendship.getId())
                .userId(friendship.getUserId())
                .friendId(friendship.getFriendId())
                .status(friendship.getStatus().name())
                .createTime(friendship.getCreateTime())
                .updateTime(friendship.getUpdateTime())
                .build();
    }
} 