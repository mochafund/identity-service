package com.mochafund.identityservice.workspace.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mochafund.identityservice.common.annotations.PatchableField;
import com.mochafund.identityservice.common.entity.BaseEntity;
import com.mochafund.identityservice.common.patchable.Patchable;
import com.mochafund.identityservice.workspace.enums.WorkspaceStatus;
import com.mochafund.identityservice.workspace.membership.entity.WorkspaceMembership;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@DynamicUpdate
@Entity
@Table(name = "workspaces")
public class Workspace extends BaseEntity implements Patchable {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceStatus status = WorkspaceStatus.PROVISIONING;

    @JsonIgnore
    @OneToMany(mappedBy = "workspace")
    @Builder.Default
    private List<WorkspaceMembership> memberships = new ArrayList<>();
}
