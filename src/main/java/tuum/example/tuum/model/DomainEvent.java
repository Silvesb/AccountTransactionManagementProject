package tuum.example.tuum.model;

import lombok.Data;

import java.time.Instant;

@Data
public class DomainEvent {
    private String eventType;
    private String entityType;
    private Instant timestamp;
    private Object payload;
}
