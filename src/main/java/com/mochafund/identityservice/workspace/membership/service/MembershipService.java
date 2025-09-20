package com.mochafund.identityservice.workspace.membership.service;

import com.mochafund.identityservice.common.exception.BadRequestException;
import com.mochafund.identityservice.common.exception.ConflictException;
import com.mochafund.identityservice.common.exception.ResourceNotFoundException;
import com.mochafund.identityservice.kafka.KafkaProducer;
import com.mochafund.identityservice.role.enums.Role;
import com.mochafund.identityservice.user.entity.User;
import com.mochafund.identityservice.user.repository.IUserRepository;
import com.mochafund.identityservice.workspace.entity.Workspace;
import com.mochafund.identityservice.workspace.membership.dto.UpdateMembershipDto;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import com.mochafund.identityservice.workspace.membership.enums.MembershipStatus;
import com.mochafund.identityservice.workspace.membership.events.WorkspaceMembershipEvent;
import com.mochafund.identityservice.workspace.membership.repository.IMembershipRepository;
import com.mochafund.identityservice.workspace.repository.IWorkspaceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
public class MembershipService implements IMembershipService {

    private final IMembershipRepository membershipRepository;
    private final IWorkspaceRepository workspaceRepository;
    private final IUserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllUserMemberships(UUID userId) {
        return membershipRepository.findAllByUser_Id(userId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceMembership> listAllWorkspaceMemberships(UUID workspaceId) {
        return membershipRepository.findAllByWorkspace_Id(workspaceId);
    }

    @Transactional
    public WorkspaceMembership createMembership(UUID userId, UUID workspaceId, Set<Role> roles) {
        log.info("Adding user {} to workspace {} with roles {}", userId, workspaceId, roles);
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (membershipRepository.findByUser_IdAndWorkspace_Id(userId, workspaceId).isPresent()) {
            throw new ConflictException("User already has a membership to workspace");
        }

        WorkspaceMembership membership = membershipRepository.save(WorkspaceMembership.builder()
                .user(user)
                .workspace(workspace)
                .roles(roles)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());

        publishEvent("workspace.membership.created", membership);

        return membership;
    }

    @Transactional
    public WorkspaceMembership updateMembership(UUID userId, UUID workspaceId, UpdateMembershipDto membershipDto) {
        log.info("Updating membership with ID: {}", workspaceId);

        WorkspaceMembership membership = membershipRepository
                .findByUser_IdAndWorkspace_Id(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have a membership to workspace"));
        membership.patchFrom(membershipDto);
        WorkspaceMembership updatedMembership = membershipRepository.save(membership);
        publishEvent("workspace.membership.updated", updatedMembership);

        return updatedMembership;
    }

    @Transactional
    public void deleteMembership(UUID userId, UUID workspaceId, boolean force) {
        WorkspaceMembership membership =  membershipRepository.findByUser_IdAndWorkspace_Id(userId, workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not have a membership to workspace"));

        if (!force) {
            long total = membershipRepository.findAllByUser_Id(userId).size();
            if (total <= 1) {
                throw new BadRequestException("User can't be removed from their only workspace");
            }
        }

        membershipRepository.deleteByUser_IdAndWorkspace_Id(userId, workspaceId);
        publishEvent("workspace.membership.deleted", membership);
    }

    private void publishEvent(String type, WorkspaceMembership membership) {
        kafkaProducer.send(WorkspaceMembershipEvent.builder()
                    .type(type)
                    .data(WorkspaceMembershipEvent.Data.builder()
                            .userId(membership.getUser().getId())
                            .workspaceId(membership.getWorkspace().getId())
                            .roles(membership.getRoles())
                            .status(membership.getStatus())
                            .joinedAt(membership.getJoinedAt())
                            .build())
                    .build()
        );
    }
}
