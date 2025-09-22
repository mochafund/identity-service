package com.mochafund.identityservice.workspace.events;

import com.mochafund.identityservice.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceEvent extends BaseEvent {
    private Data data;

    @Builder
    public record Data (
        UUID workspaceId,
        String name,
        String status
    ) {}
}