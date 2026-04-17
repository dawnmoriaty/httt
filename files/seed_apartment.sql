-- =============================================================================
-- SEED: Permissions cho 5 phân hệ quản lý chung cư mini
-- Append vào file seed.sql hiện có (sau block permissions của 'rbac' và 'sample')
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. FACILITY — Cơ sở vật chất (phòng, tài sản)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
  ('facility', 'Cơ sở vật chất', 'room',  'Phòng',    'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'room',  'Phòng',    'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'room',  'Phòng',    'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'room',  'Phòng',    'DELETE', 'Xoá',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'asset', 'Tài sản',  'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'asset', 'Tài sản',  'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'asset', 'Tài sản',  'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('facility', 'Cơ sở vật chất', 'asset', 'Tài sản',  'DELETE', 'Xoá',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 2. TENANT — Khách hàng (nhóm thuê, thành viên)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
  ('tenant', 'Khách hàng', 'tenant_group',  'Nhóm thuê',   'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_group',  'Nhóm thuê',   'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_group',  'Nhóm thuê',   'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_group',  'Nhóm thuê',   'DELETE', 'Xoá',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_member', 'Thành viên',  'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_member', 'Thành viên',  'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_member', 'Thành viên',  'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('tenant', 'Khách hàng', 'tenant_member', 'Thành viên',  'DELETE', 'Xoá',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 3. CONTRACT — Hợp đồng
-- -----------------------------------------------------------------------------
INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
  ('contract', 'Hợp đồng', 'contract', 'Hợp đồng', 'VIEW',      'Xem',         1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('contract', 'Hợp đồng', 'contract', 'Hợp đồng', 'ADD',       'Thêm',        1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('contract', 'Hợp đồng', 'contract', 'Hợp đồng', 'UPDATE',    'Sửa',         1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('contract', 'Hợp đồng', 'contract', 'Hợp đồng', 'TERMINATE', 'Chấm dứt',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('contract', 'Hợp đồng', 'contract', 'Hợp đồng', 'EXPORT',    'Xuất file',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 4. SERVICE — Dịch vụ (loại dịch vụ, ghi chỉ số)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
  ('service', 'Dịch vụ', 'service_type',  'Loại dịch vụ', 'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_type',  'Loại dịch vụ', 'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_type',  'Loại dịch vụ', 'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_type',  'Loại dịch vụ', 'DELETE', 'Xoá',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_usage', 'Ghi chỉ số',   'VIEW',   'Xem',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_usage', 'Ghi chỉ số',   'ADD',    'Thêm',   1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('service', 'Dịch vụ', 'service_usage', 'Ghi chỉ số',   'UPDATE', 'Sửa',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

-- -----------------------------------------------------------------------------
-- 5. FINANCE — Tài chính (hoá đơn, thanh toán)
-- -----------------------------------------------------------------------------
INSERT INTO permissions (module_code, module_name, resource_code, resource_name, action_code, action_name, status, created_at, updated_at)
VALUES
  ('finance', 'Tài chính', 'invoice', 'Hoá đơn',      'VIEW',   'Xem',          1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'invoice', 'Hoá đơn',      'ADD',    'Tạo HĐ',       1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'invoice', 'Hoá đơn',      'UPDATE', 'Sửa',          1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'invoice', 'Hoá đơn',      'CANCEL', 'Huỷ',          1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'invoice', 'Hoá đơn',      'EXPORT', 'Xuất file',    1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'payment', 'Thanh toán',   'VIEW',   'Xem',          1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  ('finance', 'Tài chính', 'payment', 'Thanh toán',   'ADD',    'Ghi nhận TT',  1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (resource_code, action_code) DO NOTHING;

-- =============================================================================
-- Gán quyền cho role MANAGER: toàn bộ 5 phân hệ mới (trừ DELETE facility/tenant)
-- =============================================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'MANAGER'
  AND p.module_code IN ('facility', 'tenant', 'contract', 'service', 'finance')
  AND NOT (p.resource_code IN ('room', 'asset', 'tenant_group', 'tenant_member') AND p.action_code = 'DELETE')
ON CONFLICT DO NOTHING;

-- =============================================================================
-- Gán quyền cho role USER: chỉ VIEW ở tất cả phân hệ mới
-- =============================================================================
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.module_code IN ('facility', 'tenant', 'contract', 'service', 'finance')
WHERE r.code = 'USER'
  AND p.action_code = 'VIEW'
ON CONFLICT DO NOTHING;

-- SUPER_ADMIN tự được gán toàn bộ qua cross join trong seed ban đầu.
-- Nếu đã chạy seed trước, thêm lệnh sau để sync:
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.code = 'SUPER_ADMIN'
  AND p.module_code IN ('facility', 'tenant', 'contract', 'service', 'finance')
ON CONFLICT DO NOTHING;
