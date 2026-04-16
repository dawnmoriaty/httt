INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
('rbac', 'RBAC', 'role', 'Role', 'VIEW', 'Xem', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'role', 'Role', 'ADD', 'Them', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'role', 'Role', 'UPDATE', 'Sua', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'role', 'Role', 'DELETE', 'Xoa', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'permission', 'Permission', 'VIEW', 'Xem', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'user', 'User', 'VIEW', 'Xem', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'user', 'User', 'ADD', 'Them', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('rbac', 'RBAC', 'user', 'User', 'UPDATE', 'Sua', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'VIEW', 'Xem', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'ADD', 'Them', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'UPDATE', 'Sua', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'DELETE', 'Xoa', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'IMPORT', 'Nhap', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('sample', 'Sample Data', 'subscription', 'Subscription', 'EXPORT', 'Xuat', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

INSERT INTO roles (code, name, description, status, system_role, created_at, updated_at)
VALUES
('SUPER_ADMIN', 'Super Admin', 'Super Admin default role', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('MANAGER', 'Manager', 'Manager default role', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('USER', 'User', 'User default role', 1, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
ON CONFLICT DO NOTHING;

UPDATE subscriptions
SET owner_user_id = (
    SELECT id FROM users WHERE username = 'admin' LIMIT 1
)
WHERE owner_user_id IS NULL;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource_code = 'subscription'
WHERE r.code = 'MANAGER'
  AND p.action_code IN ('VIEW', 'ADD', 'UPDATE', 'DELETE', 'IMPORT', 'EXPORT')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.resource_code = 'subscription'
WHERE r.code = 'USER'
  AND p.action_code IN ('VIEW', 'IMPORT', 'EXPORT')
ON CONFLICT DO NOTHING;
