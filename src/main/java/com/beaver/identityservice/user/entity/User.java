package com.beaver.identityservice.user.entity;

import com.beaver.identityservice.common.annotations.PatchableField;
import com.beaver.identityservice.common.entity.BaseEntity;
import com.beaver.identityservice.common.patchable.Patchable;
import com.beaver.identityservice.workspace.membership.entity.WorkspaceMembership;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Patchable {

    @PatchableField
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @PatchableField
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "last_workspace_id")
    private UUID lastWorkspaceId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkspaceMembership> memberships = new ArrayList<>();
}