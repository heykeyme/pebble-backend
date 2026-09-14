package com.todo.nwk.config;

import com.todo.common.messaging.NotificationRouting;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NotificationRouting.EXCHANGE, true, false);
    }

    @Bean
    public Queue otpEmailQueue() {
        return new Queue(NotificationRouting.OTP_QUEUE, true);
    }

    @Bean
    public Binding otpEmailBinding(Queue otpEmailQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(otpEmailQueue)
                .to(notificationExchange)
                .with(NotificationRouting.OTP_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
