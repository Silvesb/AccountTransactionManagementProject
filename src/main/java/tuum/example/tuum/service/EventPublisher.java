package tuum.example.tuum.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tuum.example.tuum.config.RabbitConfig;
import tuum.example.tuum.model.DomainEvent;

import java.time.Instant;

@Service
public class EventPublisher {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void send(String eventType, String entityType, Object payload) {
        DomainEvent event = new DomainEvent();
        event.setEventType(eventType);
        event.setEntityType(entityType);
        event.setTimestamp(Instant.now());
        event.setPayload(payload);

        try {
            String json = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error with RabbitMQ", e);
        }
    }
}
