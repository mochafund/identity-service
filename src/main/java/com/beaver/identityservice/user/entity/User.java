package com.beaver.identityservice.user.entity;

import com.beaver.identityservice.common.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@Table(name = "users")
public class User extends BaseEntity {

    @Column("email")
    private String email;

    @Builder.Default
    @Column("is_active")
    private Boolean isActive = true;

    @Column("name")
    private String name;
}