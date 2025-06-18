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
@Table(name = "friendships", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
})
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "requester_id", nullable = false)
    private Long userId;
    
    @Column(name = "addressee_id", nullable = false)
    private Long friendId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ENUM('PENDING', 'ACCEPTED', 'DECLINED', 'BLOCKED')")
    private FriendshipStatus status = FriendshipStatus.PENDING;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    public enum FriendshipStatus {
        PENDING, // 待接受
        ACCEPTED, // 已接受
        DECLINED, // 已拒绝
        BLOCKED // 已拉黑
    }
} 