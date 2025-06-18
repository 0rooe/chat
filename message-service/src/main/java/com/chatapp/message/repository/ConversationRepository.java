package com.chatapp.message.repository;

import com.chatapp.message.model.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 会话仓库接口
 */
@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    
    /**
     * 根据用户ID和好友ID查找会话
     */
    Optional<Conversation> findByUserIdAndFriendId(Long userId, Long friendId);
    
    /**
     * 获取用户的所有会话，按最后消息时间倒序排列
     */
    List<Conversation> findAllByUserIdOrderByLastMessageTimeDesc(Long userId);
    
    /**
     * 根据用户ID和未读状态查找会话
     */
    List<Conversation> findByUserIdAndUnreadTrue(Long userId);
    
    /**
     * 统计用户未读会话数量
     */
    Long countByUserIdAndUnreadTrue(Long userId);
} 