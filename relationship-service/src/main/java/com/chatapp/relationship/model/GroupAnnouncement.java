package com.chatapp.relationship.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_announcements")
public class GroupAnnouncement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @NotBlank
    @Size(max = 200)
    @Column(nullable = false)
    private String title;
    
    @NotBlank
    @Size(max = 2000)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "publisher_id", nullable = false)
    private Long publisherId;
    
    @Column(name = "publisher_name")
    private String publisherName;
    
    @Column(name = "publish_time", nullable = false)
    private LocalDateTime publishTime;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
} 