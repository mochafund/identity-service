package com.mochafund.identityservice.workspace.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mochafund.identityservice.common.events.EventEnvelope;
import com.mochafund.identityservice.common.events.EventType;
import com.mochafund.identityservice.common.util.CorrelationIdUtil;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.events.WorkspaceEventPayload;
import com.mochafund.identityservice.workspace.membership.dto.UpdateMembershipDto;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.events.WorkspaceMembershipEventPayload;
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
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = EventType.WORKSPACE_CREATED, groupId = "identity-service")
    public void handleWorkspaceCreated(String message) {
        EventEnvelope<WorkspaceEventPayload> event = readEnvelope(message, WorkspaceEventPayload.class);
        CorrelationIdUtil.executeWithCorrelationId(event, () -> {
            WorkspaceEventPayload payload = event.getPayload();
            log.info("Processing workspace.created - Workspace: {}", payload.getName());

            Workspace workspace = workspaceService.getWorkspace(payload.getWorkspaceId());
            workspace.setStatus(WorkspaceStatus.ACTIVE);
            workspaceRepository.save(workspace);

            log.info("Successfully updated workspace status to active: {}", workspace.getId());
        });
    }

    @KafkaListener(topics = EventType.WORKSPACE_MEMBERSHIP_DELETED, groupId = "identity-service")
    public void handleMembershipDeleted(String message) {
        EventEnvelope<WorkspaceMembershipEventPayload> event = readEnvelope(message, WorkspaceMembershipEventPayload.class);
        CorrelationIdUtil.executeWithCorrelationId(event, () -> {
            WorkspaceMembershipEventPayload payload = event.getPayload();
            log.info("Processing workspace.membership.deleted - User: {}, Workspace: {}",
                payload.getUserId(), payload.getWorkspaceId());

            UUID workspaceId = payload.getWorkspaceId();
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

    private <T> EventEnvelope<T> readEnvelope(String message, Class<T> payloadType) {
        try {
            return objectMapper.readValue(
                    message,
                    objectMapper.getTypeFactory().constructParametricType(EventEnvelope.class, payloadType)
            );
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse event envelope", e);
        }
    }
}