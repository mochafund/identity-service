package com.mochafund.identityservice.workspace.consumer;

import com.mochafund.identityservice.common.events.WorkspaceMembershipEvent;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.service.IMembershipService;
import com.mochafund.identityservice.workspace.service.IWorkspaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class WorkspaceMembershipDeletedConsumer {

    private final IMembershipService membershipService;
    private final IWorkspaceService workspaceService;

    @KafkaListener(topics = "workspace.membership.deleted", groupId = "identity-service")
    public void handleMembershipDeleted(WorkspaceMembershipEvent event) {
        log.info("Processing workspace membership deletion - User: {}, Workspace: {}", 
            event.getData().userId(), event.getData().workspaceId());
        
        UUID workspaceId = event.getData().workspaceId();
        List<WorkspaceMembership> remainingMemberships = membershipService.listAllWorkspaceMemberships(workspaceId);
        
        if (remainingMemberships.isEmpty()) {
            log.info("Workspace {} has no remaining members, deleting workspace", workspaceId);
            workspaceService.deleteWorkspace(workspaceId);
            log.info("Successfully deleted empty workspace {}", workspaceId);
        } else {
            // TODO: Promote most senior member to OWNER, READ, WRITE roles
            log.info("Workspace {} still has {} remaining members, keeping workspace", 
                workspaceId, remainingMemberships.size());
        }
    }
}