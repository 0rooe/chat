package com.chatapp.relationship.dto;

import com.chatapp.relationship.model.Group;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupDto {
    private Long id;
    
    @NotBlank(message = "群组名称不能为空")
    @Size(max = 100, message = "群组名称最多100个字符")
    private String name;
    
    @Size(max = 500, message = "群组描述最多500个字符")
    private String description;
    
    private Long ownerId;
    private String ownerName; // 创建者用户名
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer maxMembers;
    private Long memberCount; // 成员数量
    
    public static GroupDto fromEntity(Group group) {
        return GroupDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .ownerId(group.getOwnerId())
                .avatar(group.getAvatar())
                .createTime(group.getCreateTime())
                .updateTime(group.getUpdateTime())
                .maxMembers(group.getMaxMembers())
                .build();
    }
} 