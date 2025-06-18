package com.chatapp.message.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    // 交换机名称
    public static final String CHAT_EXCHANGE = "chat.exchange";
    
    // 队列名称
    public static final String MESSAGE_QUEUE = "chat.message.queue";
    public static final String NOTIFICATION_QUEUE = "chat.notification.queue";
    public static final String MESSAGE_DELIVERY_QUEUE = "chat.message.delivery.queue";
    public static final String BATCH_UPDATE_QUEUE = "chat.batch.update.queue";
    
    // 路由键
    public static final String MESSAGE_ROUTING_KEY = "message.send";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.send";
    public static final String DELIVERY_ROUTING_KEY = "message.delivery";
    public static final String BATCH_UPDATE_ROUTING_KEY = "message.batch.update";

    /**
     * 声明主题交换机
     */
    @Bean
    public TopicExchange chatExchange() {
        return new TopicExchange(CHAT_EXCHANGE);
    }

    /**
     * 声明消息队列
     */
    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(MESSAGE_QUEUE).build();
    }

    /**
     * 声明通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    /**
     * 声明消息送达队列
     */
    @Bean
    public Queue messageDeliveryQueue() {
        return QueueBuilder.durable(MESSAGE_DELIVERY_QUEUE).build();
    }

    /**
     * 声明批量更新队列
     */
    @Bean
    public Queue batchUpdateQueue() {
        return QueueBuilder.durable(BATCH_UPDATE_QUEUE).build();
    }

    /**
     * 绑定消息队列到交换机
     */
    @Bean
    public Binding messageBinding() {
        return BindingBuilder.bind(messageQueue())
                .to(chatExchange())
                .with(MESSAGE_ROUTING_KEY);
    }

    /**
     * 绑定通知队列到交换机
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(chatExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * 绑定消息送达队列到交换机
     */
    @Bean
    public Binding deliveryBinding() {
        return BindingBuilder.bind(messageDeliveryQueue())
                .to(chatExchange())
                .with(DELIVERY_ROUTING_KEY);
    }

    /**
     * 绑定批量更新队列到交换机
     */
    @Bean
    public Binding batchUpdateBinding() {
        return BindingBuilder.bind(batchUpdateQueue())
                .to(chatExchange())
                .with(BATCH_UPDATE_ROUTING_KEY);
    }

    /**
     * 配置消息转换器
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 配置RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }

    /**
     * 配置监听器容器工厂
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        return factory;
    }
} 