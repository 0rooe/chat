package com.chatapp.relationship.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
public class GroupMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "group_id", nullable = false)
    private Long groupId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;
    
    @Column(name = "join_time", nullable = false)
    private LocalDateTime joinTime;
    
    @Column(name = "last_active_time")
    private LocalDateTime lastActiveTime;
    
    @Builder.Default
    private boolean muted = false;
    
    public enum MemberRole {
        OWNER, // 群主
        ADMIN, // 管理员
        MEMBER // 普通成员
    }
} 