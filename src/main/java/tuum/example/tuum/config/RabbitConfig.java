package tuum.example.tuum.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_NAME = "account.exchange";
    public static final String QUEUE_NAME = "account.events";
    public static final String ROUTING_KEY = "account.events";

    @Bean
    public DirectExchange accountExchange() {
        return new DirectExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue accountEventsQueue() {
        return new Queue(QUEUE_NAME);
    }

    @Bean
    public Binding accountEventsBinding(Queue accountEventsQueue, DirectExchange accountExchange) {
        return BindingBuilder.bind(accountEventsQueue).to(accountExchange).with(ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}
