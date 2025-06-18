package com.chatapp.message.service;

import com.chatapp.message.dto.MessageDTO;
import com.chatapp.message.dto.EncryptedMessageDto;
import com.chatapp.message.dto.MessageQueryParams;
import com.chatapp.message.model.Message;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 消息服务接口
 */
public interface MessageService {

    /**
     * 保存消息（不发送到MQ）
     * 
     * @param messageDto 消息DTO
     * @return 保存后的消息
     */
    MessageDTO saveMessage(MessageDTO messageDto);
    
    /**
     * 发送消息
     * 
     * @param messageDto 消息DTO
     * @return 发送后的消息
     */
    MessageDTO sendMessage(MessageDTO messageDto);
    
    /**
     * 获取消息详情
     * 
     * @param messageId 消息ID
     * @return 消息DTO
     */
    MessageDTO getMessageById(String messageId);
    
    /**
     * 更新消息状态
     * 
     * @param messageId 消息ID
     * @param status 新状态
     * @return 更新后的消息
     */
    MessageDTO updateMessageStatus(String messageId, Message.MessageStatus status);
    
    /**
     * 批量更新消息状态
     * 
     * @param messageIds 消息ID列表
     * @param status 新状态
     * @return 更新的消息数量
     */
    int updateMessageStatusBatch(List<String> messageIds, Message.MessageStatus status);
    
    /**
     * 查询私聊历史记录
     * 
     * @param userId1 用户1 ID
     * @param userId2 用户2 ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页消息列表
     */
    Page<MessageDTO> getPrivateMessages(Long userId1, Long userId2, int page, int size);
    
    /**
     * 查询群聊历史记录
     * 
     * @param groupId 群组ID
     * @param page 页码
     * @param size 每页大小
     * @return 分页消息列表
     */
    Page<MessageDTO> getGroupMessages(Long groupId, int page, int size);
    
    /**
     * 根据复杂查询条件查询消息
     * 
     * @param queryParams 查询参数
     * @return 分页消息列表
     */
    Page<MessageDTO> getMessagesByQueryParams(MessageQueryParams queryParams);
    
    /**
     * 获取用户未读消息
     * 
     * @param userId 用户ID
     * @return 未读消息列表
     */
    List<MessageDTO> getUnreadMessages(Long userId);
    
    /**
     * 标记来自指定发送者的消息为已读
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @return 更新的消息数量
     */
    int markMessagesAsRead(Long receiverId, Long senderId);
    
    /**
     * 标记用户所有未读消息为已读
     * 
     * @param userId 用户ID
     * @return 更新的消息数量
     */
    int markAllMessagesAsRead(Long userId);
    
    /**
     * 删除消息
     * 
     * @param messageId 消息ID
     */
    void deleteMessage(String messageId);
    
    /**
     * 撤回消息（需要校验是否在可撤回时间内）
     * 
     * @param messageId 消息ID
     * @param userId 撤回用户ID（必须是消息发送者）
     * @return 撤回后的消息
     */
    MessageDTO recallMessage(String messageId, Long userId);
    
    MessageDTO saveEncryptedMessage(EncryptedMessageDto encryptedMessageDto);
    
    List<MessageDTO> searchMessages(String keyword, Long userId, int page, int size);
} 