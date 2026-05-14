package com.ande.pubquizzz.database.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Entity
@Table(name = "app_usage_event", indexes = {
        @Index(name = "idx_usage_event_type_time", columnList = "eventType, occurredAt"),
        @Index(name = "idx_usage_event_user_time", columnList = "username, occurredAt")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class UsageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long usageEventId;

    @Column(nullable = false, length = 64)
    private String eventType;

    @Column(nullable = false, length = 255)
    private String username;

    @Column(nullable = false)
    private Instant occurredAt;

    @Column(length = 64)
    private String entityType;

    @Column(length = 128)
    private String entityId;

    @Lob
    private String metadataJson;
}
