package com.chatapp.relationship.dto;

import com.chatapp.relationship.model.GroupAnnouncement;
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
public class GroupAnnouncementDto {
    private Long id;
    
    private Long groupId;
    
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题最多200个字符")
    private String title;
    
    @NotBlank(message = "公告内容不能为空")
    @Size(max = 2000, message = "公告内容最多2000个字符")
    private String content;
    
    private Long publisherId;
    private String publisherName;
    private LocalDateTime publishTime;
    private Boolean isActive;
    
    public static GroupAnnouncementDto fromEntity(GroupAnnouncement announcement) {
        if (announcement == null) {
            return null;
        }
        
        return GroupAnnouncementDto.builder()
                .id(announcement.getId())
                .groupId(announcement.getGroupId())
                .title(announcement.getTitle())
                .content(announcement.getContent())
                .publisherId(announcement.getPublisherId())
                .publisherName(announcement.getPublisherName())
                .publishTime(announcement.getPublishTime())
                .isActive(announcement.getIsActive())
                .build();
    }
} 