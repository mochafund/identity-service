package com.mochafund.identityservice.workspace.membership.repository;

import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {
    List<WorkspaceMembership> findAllByUser_Id(UUID userId);
    Optional<WorkspaceMembership> findByUser_IdAndWorkspace_Id(UUID userId, UUID workspaceId);
    boolean existsByUser_IdAndWorkspace_Id(UUID userId, UUID workspaceId);
    long countByUser_Id(UUID userId);
    void deleteByUser_IdAndWorkspace_Id(UUID userId, UUID workspaceId);
}
