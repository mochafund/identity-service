package com.mochafund.identityservice.common.events;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class BaseEvent {
    private UUID id = UUID.randomUUID();
    private LocalDateTime publishedAt = LocalDateTime.now();
    private String type;
    private String actor;
    private String actorType;
}
