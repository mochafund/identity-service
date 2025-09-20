package com.mochafund.identityservice.workspace.membership.repository;

import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IMembershipRepository extends JpaRepository<WorkspaceMembership, UUID> {

    @EntityGraph(attributePaths = {"workspace"})
    List<WorkspaceMembership> findAllByUser_Id(UUID userId);

    @EntityGraph(attributePaths = {"user"})
    List<WorkspaceMembership> findAllByWorkspace_Id(UUID workspaceId);

    @EntityGraph(attributePaths = {"user", "workspace"})
    Optional<WorkspaceMembership> findByUser_IdAndWorkspace_Id(UUID userId, UUID workspaceId);

    void deleteByUser_IdAndWorkspace_Id(UUID userId, UUID workspaceId);
}
