package com.mochafund.identityservice.workspace.membership.events;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class WorkspaceMembershipEventPayload {
    private UUID userId;
    private UUID workspaceId;
    private Set<Role> roles;
    private MembershipStatus status;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime joinedAt;
}
