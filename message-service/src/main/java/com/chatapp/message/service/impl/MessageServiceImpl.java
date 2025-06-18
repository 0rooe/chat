package com.chatapp.message.service.impl;

import com.chatapp.message.dto.MessageDTO;
import com.chatapp.message.dto.EncryptedMessageDto;
import com.chatapp.message.dto.MessageQueryParams;
import com.chatapp.message.exception.ResourceNotFoundException;
import com.chatapp.message.model.Message;
import com.chatapp.message.repository.MessageRepository;
import com.chatapp.message.service.MessageService;
import com.chatapp.message.service.EncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import com.chatapp.message.config.RabbitMQConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 消息服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MongoTemplate mongoTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final EncryptionService encryptionService;
    
    // RabbitMQ路由键
    private static final String PRIVATE_MESSAGE_ROUTING_KEY = "message.send.private";
    private static final String GROUP_MESSAGE_ROUTING_KEY = "message.send.group";
    private static final Duration RECALL_TIME_LIMIT = Duration.ofMinutes(2); // 2分钟内可撤回
    
    @Override
    public MessageDTO saveMessage(MessageDTO messageDto) {
        Message message = Message.builder()
                .senderId(messageDto.getSenderId())
                .receiverId(messageDto.getReceiverId())
                .content(messageDto.getContent())
                .contentType(Message.ContentType.valueOf(messageDto.getContentType()))
                .messageType(Message.MessageType.valueOf(messageDto.getMessageType()))
                .status(Message.MessageStatus.SENT)
                .isEncrypted(messageDto.isEncrypted())
                .createTime(LocalDateTime.now())
                .build();
                
        Message savedMessage = messageRepository.save(message);
        log.info("消息已保存: {}", savedMessage.getId());
        
        return MessageDTO.fromEntity(savedMessage);
    }

    @Override
    public MessageDTO sendMessage(MessageDTO messageDto) {
        Message message = messageDto.toEntity();
        
        // 设置初始状态
        if (message.getStatus() == null) {
            message.setStatus(Message.MessageStatus.SENDING);
        }
        
        // 设置创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        message.setCreateTime(now);
        message.setUpdateTime(now);
        
        // 保存消息
        Message savedMessage = messageRepository.save(message);
        log.info("消息已保存: {}", savedMessage.getId());
        
        // 通过RabbitMQ发送消息，根据消息类型选择不同的路由键
        String routingKey = message.getMessageType() == Message.MessageType.PRIVATE ? 
            PRIVATE_MESSAGE_ROUTING_KEY : GROUP_MESSAGE_ROUTING_KEY;
        
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, routingKey, MessageDTO.fromEntity(savedMessage));
        log.info("消息已发送到RabbitMQ, 交换机: {}, 路由键: {}", RabbitMQConfig.CHAT_EXCHANGE, routingKey);
        
        // 更新消息状态为已发送
        savedMessage.setStatus(Message.MessageStatus.SENT);
        savedMessage = messageRepository.save(savedMessage);
        
        return MessageDTO.fromEntity(savedMessage);
    }
    
    @Override
    public MessageDTO getMessageById(String messageId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在: " + messageId));
                
        return MessageDTO.fromEntity(message);
    }
    
    @Override
    public MessageDTO updateMessageStatus(String messageId, Message.MessageStatus status) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在: " + messageId));
                
        message.setStatus(status);
        message.setUpdateTime(LocalDateTime.now());
        
        Message updatedMessage = messageRepository.save(message);
        log.info("消息状态已更新: {}, 新状态: {}", messageId, status);
        
        return MessageDTO.fromEntity(updatedMessage);
    }
    
    @Override
    public int updateMessageStatusBatch(List<String> messageIds, Message.MessageStatus status) {
        Query query = new Query(Criteria.where("id").in(messageIds));
        Update update = new Update()
                .set("status", status)
                .set("updateTime", LocalDateTime.now());
                
        long count = mongoTemplate.updateMulti(query, update, Message.class).getModifiedCount();
        log.info("已批量更新 {} 条消息状态为 {}", count, status);
        
        return (int) count;
    }
    
    @Override
    public Page<MessageDTO> getPrivateMessages(Long userId1, Long userId2, int page, int size) {
        log.info("使用MongoTemplate查询用户 {} 和用户 {} 的私聊历史", userId1, userId2);
        
        try {
            // 使用MongoTemplate构建查询
            Query query = new Query();
            
            // 构建查询条件：(senderId=userId1 AND receiverId=userId2) OR (senderId=userId2 AND receiverId=userId1)
            Criteria criteria = new Criteria().orOperator(
                Criteria.where("senderId").is(userId1).and("receiverId").is(userId2).and("messageType").is(Message.MessageType.PRIVATE),
                Criteria.where("senderId").is(userId2).and("receiverId").is(userId1).and("messageType").is(Message.MessageType.PRIVATE)
            );
            
            query.addCriteria(criteria);
            query.with(Sort.by(Sort.Direction.DESC, "createTime"));
            
            // 分页
            int skip = page * size;
            query.skip(skip).limit(size);
            
            // 执行查询
            List<Message> messages = mongoTemplate.find(query, Message.class);
            long total = mongoTemplate.count(query, Message.class);
            
            log.info("查询到 {} 条私聊历史消息，总数: {}", messages.size(), total);
            
            // 转换为DTO
            List<MessageDTO> messageDTOs = messages.stream()
                    .map(MessageDTO::fromEntity)
                    .collect(Collectors.toList());
            
            // 创建分页结果
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
            return new PageImpl<>(messageDTOs, pageRequest, total);
            
        } catch (Exception e) {
            log.error("查询私聊消息失败: {}", e.getMessage(), e);
            // 返回空页面
            PageRequest pageRequest = PageRequest.of(page, size);
            return new PageImpl<>(new ArrayList<>(), pageRequest, 0);
        }
    }
    
    @Override
    public Page<MessageDTO> getGroupMessages(Long groupId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        
        Page<Message> messagePage = messageRepository.findByMessageTypeAndReceiverIdOrderByCreateTimeDesc(
                Message.MessageType.GROUP, groupId, pageRequest);
                
        return messagePage.map(MessageDTO::fromEntity);
    }
    
    @Override
    public Page<MessageDTO> getMessagesByQueryParams(MessageQueryParams queryParams) {
        // 构建查询条件
        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();
        
        if (queryParams.getSenderId() != null) {
            criteriaList.add(Criteria.where("senderId").is(queryParams.getSenderId()));
        }
        
        if (queryParams.getReceiverId() != null) {
            criteriaList.add(Criteria.where("receiverId").is(queryParams.getReceiverId()));
        }
        
        if (queryParams.getMessageType() != null) {
            criteriaList.add(Criteria.where("messageType").is(queryParams.getMessageType()));
        }
        
        if (queryParams.getStartTime() != null && queryParams.getEndTime() != null) {
            LocalDateTime startTime = LocalDateTime.ofEpochSecond(queryParams.getStartTime() / 1000, 0, java.time.ZoneOffset.UTC);
            LocalDateTime endTime = LocalDateTime.ofEpochSecond(queryParams.getEndTime() / 1000, 0, java.time.ZoneOffset.UTC);
            criteriaList.add(Criteria.where("createTime").gte(startTime).lte(endTime));
        }
        
        // 组合条件
        if (!criteriaList.isEmpty()) {
            Criteria criteria = new Criteria();
            criteria.andOperator(criteriaList.toArray(new Criteria[0]));
            query.addCriteria(criteria);
        }
        
        // 排序和分页
        int page = queryParams.getPage() != null ? queryParams.getPage() : 0;
        int size = queryParams.getSize() != null ? queryParams.getSize() : 20;
        
        query.with(Sort.by(Sort.Direction.DESC, "createTime"));
        query.skip((long) page * size);
        query.limit(size);
        
        // 执行查询
        long total = mongoTemplate.count(query, Message.class);
        List<Message> messages = mongoTemplate.find(query, Message.class);
        
        List<MessageDTO> messageDtos = messages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
                
        // 创建自定义Page对象
        return new org.springframework.data.domain.PageImpl<>(messageDtos, PageRequest.of(page, size), total);
    }
    
    @Override
    public List<MessageDTO> getUnreadMessages(Long userId) {
        List<Message> unreadMessages = messageRepository.findByReceiverIdAndStatusNot(userId, Message.MessageStatus.READ);
        
        return unreadMessages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public int markMessagesAsRead(Long receiverId, Long senderId) {
        Query query = new Query(
                Criteria.where("receiverId").is(receiverId)
                        .and("senderId").is(senderId)
                        .and("status").ne(Message.MessageStatus.READ)
        );
        
        Update update = new Update()
                .set("status", Message.MessageStatus.READ)
                .set("updateTime", LocalDateTime.now());
        
        long count = mongoTemplate.updateMulti(query, update, Message.class).getModifiedCount();
        log.info("已标记用户 {} 来自发送者 {} 的 {} 条消息为已读", receiverId, senderId, count);
        
        return (int) count;
    }
    
    @Override
    public int markAllMessagesAsRead(Long userId) {
        Query query = new Query(
                Criteria.where("receiverId").is(userId)
                        .and("status").ne(Message.MessageStatus.READ)
        );
        
        Update update = new Update()
                .set("status", Message.MessageStatus.READ)
                .set("updateTime", LocalDateTime.now());
        
        long count = mongoTemplate.updateMulti(query, update, Message.class).getModifiedCount();
        log.info("已标记用户 {} 的 {} 条消息为已读", userId, count);
        
        return (int) count;
    }
    
    @Override
    public void deleteMessage(String messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new ResourceNotFoundException("消息不存在: " + messageId);
        }
        
        messageRepository.deleteById(messageId);
        log.info("消息已删除: {}", messageId);
    }
    
    @Override
    public MessageDTO recallMessage(String messageId, Long userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("消息不存在: " + messageId));
                
        // 检查用户是否是消息发送者
        if (!message.getSenderId().equals(userId)) {
            throw new IllegalStateException("只有消息发送者可以撤回消息");
        }
        
        // 检查是否在可撤回时间范围内
        LocalDateTime now = LocalDateTime.now();
        if (Duration.between(message.getCreateTime(), now).compareTo(RECALL_TIME_LIMIT) > 0) {
            throw new IllegalStateException("消息发送超过2分钟，无法撤回");
        }
        
        // 更新消息内容为撤回状态
        message.setContent("[此消息已被撤回]");
        message.setStatus(Message.MessageStatus.FAILED);
        message.setUpdateTime(now);
        
        Message updatedMessage = messageRepository.save(message);
        log.info("消息已撤回: {}", messageId);
        
        // 发送消息撤回通知
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, "message.recall", MessageDTO.fromEntity(updatedMessage));
        
        return MessageDTO.fromEntity(updatedMessage);
    }

    @Override
    public MessageDTO saveEncryptedMessage(EncryptedMessageDto encryptedMessageDto) {
        Message message = Message.builder()
                .senderId(encryptedMessageDto.getSenderId())
                .receiverId(encryptedMessageDto.getReceiverId())
                .content(encryptedMessageDto.getEncryptedContent())
                .contentType(Message.ContentType.valueOf(encryptedMessageDto.getContentType()))
                .messageType(Message.MessageType.valueOf(encryptedMessageDto.getMessageType()))
                .status(Message.MessageStatus.SENT)
                .isEncrypted(true)
                .createTime(LocalDateTime.now())
                .build();
        
        Message savedMessage = messageRepository.save(message);
        
        // 发送消息到RabbitMQ
        String routingKey = message.getMessageType() == Message.MessageType.PRIVATE ? 
            PRIVATE_MESSAGE_ROUTING_KEY : GROUP_MESSAGE_ROUTING_KEY;
        rabbitTemplate.convertAndSend(RabbitMQConfig.CHAT_EXCHANGE, routingKey, MessageDTO.fromEntity(savedMessage));
        
        return MessageDTO.fromEntity(savedMessage);
    }
    
    @Override
    public List<MessageDTO> searchMessages(String keyword, Long userId, int page, int size) {
        // 查询用户相关的所有消息
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime"));
        Page<Message> messagePage = messageRepository.findBySenderIdOrReceiverId(userId, userId, pageRequest);
        
        // 过滤出包含关键词的消息
        Stream<Message> messageStream = messagePage.getContent().stream()
                .filter(message -> {
                    // 如果是加密消息，不进行关键词搜索
                    if (message.isEncrypted()) {
                        return false;
                    }
                    return message.getContent() != null && message.getContent().toLowerCase().contains(keyword.toLowerCase());
                });
                
        return messageStream
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 