package com.mochafund.identityservice.common.events;

import lombok.experimental.UtilityClass;

@UtilityClass
public class EventType {
    public static final String WORKSPACE_PROVISIONING = "workspace.provisioning";
    public static final String WORKSPACE_CREATED = "workspace.created";
    public static final String WORKSPACE_DELETED_INITIALIZED = "workspace.deleted.initialized";

    public static final String WORKSPACE_MEMBERSHIP_CREATED = "workspace.membership.created";
    public static final String WORKSPACE_MEMBERSHIP_UPDATED = "workspace.membership.updated";
    public static final String WORKSPACE_MEMBERSHIP_DELETED = "workspace.membership.deleted";

    public static final String USER_CREATED = "user.created";
    public static final String USER_UPDATED = "user.updated";
    public static final String USER_DELETED = "user.deleted";
}
