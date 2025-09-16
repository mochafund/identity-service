package com.mochafund.identityservice.workspace.events;

import com.mochafund.identityservice.common.events.BaseEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceMembershipEvent extends BaseEvent {
    private final Data data;

    @Builder
    public record Data(UUID userId, UUID workspaceId) {}
}
