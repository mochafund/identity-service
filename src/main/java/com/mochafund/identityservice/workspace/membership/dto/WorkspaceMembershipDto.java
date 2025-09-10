package com.mochafund.identityservice.workspace.membership.dto;

import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Getter
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class WorkspaceMembershipDto {
    private UUID userId;
    private UUID workspaceId;
    private Set<Role> roles;
    private MembershipStatus status;
    private LocalDateTime joinedAt;

    public static WorkspaceMembershipDto fromEntity(WorkspaceMembership membership) {
        return WorkspaceMembershipDto.builder()
                .userId(membership.getUser().getId())
                .workspaceId(membership.getWorkspace().getId())
                .roles(membership.getRoles())
                .status(membership.getStatus())
                .joinedAt(membership.getJoinedAt())
                .build();
    }
}