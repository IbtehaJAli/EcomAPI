package com.ibtehaj.Ecom;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Queue productStockUpdateQueue() {
        Queue queue = new Queue("product.stock.update");
        rabbitAdmin().declareQueue(queue);
        return queue;
    }
    @Bean
    public Queue checkOutQueue() {
        Queue queue = new Queue("checkout");
        rabbitAdmin().declareQueue(queue);
        return queue;
    }

    @Bean
    public RabbitTemplate rabbitTemplate() {
        return new RabbitTemplate(connectionFactory);
    }

}
