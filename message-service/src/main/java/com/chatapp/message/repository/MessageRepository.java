package com.chatapp.message.repository;

import com.chatapp.message.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 消息MongoDB数据访问接口
 */
@Repository
public interface MessageRepository extends MongoRepository<Message, String> {

    /**
     * 查询私聊消息历史
     * 私聊消息条件：(senderId=用户A AND receiverId=用户B) OR (senderId=用户B AND receiverId=用户A)
     */
    @Query("{'$or': [{'senderId': ?0, 'receiverId': ?1, 'messageType': 'PRIVATE'}, {'senderId': ?1, 'receiverId': ?0, 'messageType': 'PRIVATE'}]}")
    Page<Message> findPrivateMessagesBetweenUsers(Long userId1, Long userId2, Pageable pageable);
    
    /**
     * 查询某个用户的所有私聊消息
     */
    Page<Message> findByMessageTypeAndSenderIdOrMessageTypeAndReceiverIdOrderByCreateTimeDesc(
            Message.MessageType type1, Long senderId,
            Message.MessageType type2, Long receiverId,
            Pageable pageable);
            
    /**
     * 查询群聊消息历史
     */
    Page<Message> findByMessageTypeAndReceiverIdOrderByCreateTimeDesc(
            Message.MessageType messageType, Long receiverId, Pageable pageable);
            
    /**
     * 查询特定时间范围内的消息
     */
    Page<Message> findByCreateTimeBetweenOrderByCreateTimeDesc(
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
            
    /**
     * 查询特定时间范围内的用户或群组消息
     */
    Page<Message> findByReceiverIdAndCreateTimeBetweenOrderByCreateTimeDesc(
            Long receiverId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
            
    /**
     * 查询用户未读消息
     */
    List<Message> findByReceiverIdAndStatusNot(Long receiverId, Message.MessageStatus status);
    
    /**
     * 查询发送给某用户的最近消息
     */
    Message findFirstByReceiverIdOrderByCreateTimeDesc(Long receiverId);

    // 查找两个用户之间的私聊消息
    List<Message> findByMessageTypeAndSenderIdAndReceiverIdOrderByCreateTimeDesc(
            Message.MessageType messageType, Long senderId, Long receiverId);
    
    // 查找指定时间之后的私聊消息
    List<Message> findByMessageTypeAndSenderIdAndReceiverIdAndCreateTimeAfterOrderByCreateTimeAsc(
            Message.MessageType messageType, Long senderId, Long receiverId, LocalDateTime after);
    
    // 查找群组消息
    List<Message> findByMessageTypeAndReceiverIdOrderByCreateTimeDesc(
            Message.MessageType messageType, Long groupId);
    
    // 查找指定时间之后的群组消息
    List<Message> findByMessageTypeAndReceiverIdAndCreateTimeAfterOrderByCreateTimeAsc(
            Message.MessageType messageType, Long groupId, LocalDateTime after);
    
    // 查找用户发送或接收的所有私聊消息
    List<Message> findByMessageTypeAndSenderIdOrReceiverIdOrderByCreateTimeDesc(
            Message.MessageType messageType, Long userId, Long receiverId);

    /**
     * 查找用户发送或接收的所有消息（分页）
     */
    Page<Message> findBySenderIdOrReceiverId(Long senderId, Long receiverId, Pageable pageable);

    List<Message> findByReceiverIdAndSenderIdOrderByCreateTimeDesc(Long receiverId, Long senderId, Pageable pageable);
    List<Message> findByContentContainingIgnoreCaseAndIsEncryptedFalse(String keyword, Pageable pageable);
    List<Message> findBySenderIdAndContentContainingIgnoreCaseAndIsEncryptedFalse(Long senderId, String keyword, Pageable pageable);
    List<Message> findByReceiverIdAndContentContainingIgnoreCaseAndIsEncryptedFalse(Long receiverId, String keyword, Pageable pageable);

    /**
     * 查询用户最近的私聊消息
     * 获取用户发送或接收的最近20条私聊消息
     */
    @Query("{'$or': [{'senderId': ?0}, {'receiverId': ?0}], 'messageType': 'PRIVATE'}")
    List<Message> findRecentPrivateMessagesByUserId(Long userId, Pageable pageable);
    
    /**
     * 查询用户最近的私聊消息，不分页
     */
    default List<Message> findRecentPrivateMessagesByUserId(Long userId) {
        return findByMessageTypeAndSenderIdOrMessageTypeAndReceiverIdOrderByCreateTimeDesc(
                Message.MessageType.PRIVATE, userId, Message.MessageType.PRIVATE, userId, 
                org.springframework.data.domain.PageRequest.of(0, 20)).getContent();
    }
    
    /**
     * 查询用户参与的群聊最近消息
     */
    @Query("{'receiverId': {'$in': ?0}, 'messageType': 'GROUP'}")
    List<Message> findRecentGroupMessagesByUserGroups(List<Long> groupIds, Pageable pageable);
    
    /**
     * 查询用户参与的群聊最近消息，暂时返回所有群聊消息
     */
    default List<Message> findRecentGroupMessagesByUserId(Long userId) {
        return findByMessageTypeOrderByCreateTimeDesc(Message.MessageType.GROUP, 
                org.springframework.data.domain.PageRequest.of(0, 20)).getContent();
    }
    
    /**
     * 查询指定消息类型的所有消息
     */
    List<Message> findByMessageTypeOrderByCreateTimeDesc(Message.MessageType messageType);
    
    /**
     * 查询指定消息类型的消息（分页）
     */
    Page<Message> findByMessageTypeOrderByCreateTimeDesc(Message.MessageType messageType, Pageable pageable);
} 