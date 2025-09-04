package com.beaver.identityservice.workspace.repository;

import com.beaver.identityservice.workspace.entity.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IWorkspaceRepository extends JpaRepository<Workspace, UUID> {
}
