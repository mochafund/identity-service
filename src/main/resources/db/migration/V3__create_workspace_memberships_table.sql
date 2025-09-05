-- Workspace Memberships with roles stored directly as JSONB array
CREATE TABLE workspace_memberships (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    workspace_id UUID NOT NULL REFERENCES workspaces(id) ON DELETE CASCADE,
    roles JSONB NOT NULL DEFAULT '[]' CHECK (jsonb_typeof(roles) = 'array'),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'SUSPENDED', 'PENDING')),
    joined_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, workspace_id)
);

-- Index for querying memberships by roles
CREATE INDEX idx_workspace_memberships_roles ON workspace_memberships USING GIN (roles);

-- Add foreign key constraint for last_workspace_id now that workspaces table exists
ALTER TABLE users ADD CONSTRAINT fk_users_last_workspace
    FOREIGN KEY (last_workspace_id) REFERENCES workspaces(id) ON DELETE SET NULL;
