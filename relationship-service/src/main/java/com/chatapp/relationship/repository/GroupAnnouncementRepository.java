package com.chatapp.relationship.repository;

import com.chatapp.relationship.model.GroupAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupAnnouncementRepository extends JpaRepository<GroupAnnouncement, Long> {
    
    /**
     * 查找群组的最新公告
     */
    Optional<GroupAnnouncement> findFirstByGroupIdAndIsActiveTrueOrderByPublishTimeDesc(Long groupId);
    
    /**
     * 查找群组的所有有效公告
     */
    List<GroupAnnouncement> findByGroupIdAndIsActiveTrueOrderByPublishTimeDesc(Long groupId);
    
    /**
     * 删除群组的所有公告
     */
    void deleteByGroupId(Long groupId);
} 