package com.chatapp.relationship.repository;

import com.chatapp.relationship.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByOwnerId(Long ownerId);
    
    @Query("SELECT g FROM Group g JOIN GroupMember gm ON g.id = gm.groupId WHERE gm.userId = ?1")
    List<Group> findGroupsByUserId(Long userId);
    
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', ?1, '%'))")
    List<Group> findGroupsByNameContainingIgnoreCase(String name);
    
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.groupId = ?1")
    long countMembersByGroupId(Long groupId);
} 