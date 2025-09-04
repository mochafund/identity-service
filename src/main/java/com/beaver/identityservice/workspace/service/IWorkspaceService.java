package com.beaver.identityservice.workspace.service;

import com.beaver.identityservice.user.entity.User;
import com.beaver.identityservice.membership.entity.WorkspaceMembership;

public interface IWorkspaceService {
    WorkspaceMembership createDefaultWorkspace(User user);
}
