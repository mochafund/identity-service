package com.mochafund.identityservice.workspace.consumer;

import com.mochafund.identityservice.common.util.CorrelationIdUtil;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.events.WorkspaceEvent;
import com.mochafund.identityservice.workspace.membership.dto.UpdateMembershipDto;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.events.WorkspaceMembershipEvent;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class WorkspaceEventConsumer {

    private final IMembershipService membershipService;
    private final IWorkspaceService workspaceService;
    private final IWorkspaceRepository workspaceRepository;

    @KafkaListener(topics = "workspace.created", groupId = "identity-service")
    public void handleWorkspaceCreated(WorkspaceEvent event) {
        CorrelationIdUtil.executeWithCorrelationId(event, () -> {
            log.info("Processing workspace.created - Workspace: {}", event.getData().name());

            Workspace workspace = workspaceService.getWorkspace(event.getData().workspaceId());
            workspace.setStatus(WorkspaceStatus.ACTIVE);
            workspaceRepository.save(workspace);

            log.info("Successfully updated workspace status to active: {}", workspace.getId());
        });
    }

    @KafkaListener(topics = "workspace.membership.deleted", groupId = "identity-service")
    public void handleMembershipDeleted(WorkspaceMembershipEvent event) {
        CorrelationIdUtil.executeWithCorrelationId(event, () -> {
            log.info("Processing workspace.membership.deleted - User: {}, Workspace: {}",
                event.getData().userId(), event.getData().workspaceId());

            UUID workspaceId = event.getData().workspaceId();
            List<WorkspaceMembership> remainingMemberships = membershipService.listAllWorkspaceMemberships(workspaceId);

            if (remainingMemberships.isEmpty()) {
                log.info("Workspace {} has no remaining members, deleting workspace", workspaceId);
                workspaceService.deleteWorkspace(workspaceId);
                log.info("Successfully deleted empty workspace {}", workspaceId);
            } else {
                Optional<WorkspaceMembership> mostSeniorMember = remainingMemberships
                        .stream()
                        .min(Comparator.comparing(WorkspaceMembership::getJoinedAt));

                log.info("Workspace {} has {} remaining members, setting most senior member to owner", workspaceId, remainingMemberships.size());
                mostSeniorMember.ifPresent(m ->
                        membershipService.updateMembership(
                            m.getUser().getId(),
                            m.getWorkspace().getId(),
                                UpdateMembershipDto.builder()
                                    .roles(Set.of(Role.OWNER, Role.WRITE, Role.READ))
                                    .build()
                        )
                );
            }
        });
    }
}