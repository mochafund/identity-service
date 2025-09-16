package com.mochafund.identityservice.workspace.consumer;

import com.mochafund.identityservice.workspace.events.WorkspaceMembershipEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WorkspaceMembershipDeletedConsumer {

    @KafkaListener(topics = "workspace.membership.deleted", groupId = "identity-service")
    public void handleMembershipDeleted(WorkspaceMembershipEvent event) {
        log.info("Processing workspace membership deletion - User: {}, Workspace: {}", 
            event.getData().userId(), event.getData().workspaceId());
        
        // TODO: Add specific processing logic for membership deletion
        // For now, just log the event type as requested
        log.info("Event type: {}", event.getType());
    }
}