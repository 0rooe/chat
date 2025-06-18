package com.chatapp.relationship.repository;

import com.chatapp.relationship.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroupId(Long groupId);
    
    List<GroupMember> findByUserId(Long userId);
    
    List<GroupMember> findByGroupIdAndRole(Long groupId, GroupMember.MemberRole role);
    
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    
    long countByGroupId(Long groupId);
    
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
} 