package com.healthsys.triage.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange topicExchange(MessagingProperties properties) {
        return new TopicExchange(properties.getExchange());
    }

    @Bean
    public Queue authLogoutQueue(MessagingProperties properties) {
        return new Queue(properties.getAuthLogoutQueue(), true);
    }

    @Bean
    public Binding authLogoutBinding(
        Queue authLogoutQueue,
        TopicExchange topicExchange,
        MessagingProperties properties
    ) {
        return BindingBuilder.bind(authLogoutQueue)
            .to(topicExchange)
            .with(properties.getAuthLogoutRoutingKey());
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
