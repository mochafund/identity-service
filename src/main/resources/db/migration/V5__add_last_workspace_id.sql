-- Add last_workspace_id column to users table
ALTER TABLE users ADD COLUMN last_workspace_id UUID;

-- Add foreign key constraint (optional, but recommended for data integrity)
ALTER TABLE users
    ADD CONSTRAINT fk_users_last_workspace
        FOREIGN KEY (last_workspace_id) REFERENCES workspaces(id) ON DELETE SET NULL;

-- Create index for performance (optional, but recommended if you'll query by this field)
CREATE INDEX idx_users_last_workspace_id ON users(last_workspace_id);