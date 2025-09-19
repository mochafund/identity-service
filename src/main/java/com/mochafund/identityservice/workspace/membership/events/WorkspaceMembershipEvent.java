package com.mochafund.identityservice.workspace.membership.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mochafund.identityservice.common.events.BaseEvent;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceMembershipEvent extends BaseEvent {
    private Data data;

    @Builder
    public record Data (
        UUID userId,
        UUID workspaceId,
        Set<Role> roles,
        MembershipStatus status,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        LocalDateTime joinedAt
    ) {}
}