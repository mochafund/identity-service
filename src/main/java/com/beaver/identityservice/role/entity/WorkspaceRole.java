package com.beaver.identityservice.role.entity;

import com.beaver.identityservice.common.entity.BaseEntity;
import com.beaver.identityservice.role.enums.Role;
import com.beaver.identityservice.workspace.entity.Workspace;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@DynamicUpdate
@Table(name = "roles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"workspace_id", "role_type"})
})
public class WorkspaceRole extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workspace_id", nullable = false)
    private Workspace workspace;

    @Column(name = "role_type", nullable = false, length = 10)
    @Convert(converter = RoleConverter.class)
    private Role roleType;

    @Override
    public String toString() {
        return roleType.name();
    }

    @jakarta.persistence.Converter
    public static class RoleConverter implements AttributeConverter<Role, String> {
        @Override
        public String convertToDatabaseColumn(Role role) {
            return role != null ? role.name() : null;
        }

        @Override
        public Role convertToEntityAttribute(String dbData) {
            if (dbData == null) return null;
            return Role.valueOf(dbData);
        }
    }
}