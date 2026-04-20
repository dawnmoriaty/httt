-- Idempotent schema cleanup/optimization for the current contract-tenant model.

-- contracts
ALTER TABLE contracts
    ADD COLUMN IF NOT EXISTS deposit_amount NUMERIC(15, 2);

UPDATE contracts
SET deposit_amount = 0
WHERE deposit_amount IS NULL;

CREATE INDEX IF NOT EXISTS idx_contracts_room_status
    ON contracts(room_id, status);

CREATE INDEX IF NOT EXISTS idx_contracts_tenant_group_status
    ON contracts(tenant_group_id, status);

-- rooms
ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS max_occupants INTEGER;

ALTER TABLE rooms
    ADD COLUMN IF NOT EXISTS active_contract_id BIGINT;

CREATE INDEX IF NOT EXISTS idx_rooms_active_contract_id
    ON rooms(active_contract_id);

-- cameras
ALTER TABLE cameras
    ALTER COLUMN subscription_id DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_cameras_room_code
    ON cameras(room_id, code);

-- service_usages
ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS room_id BIGINT;

ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS previous_reading INTEGER;

ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS current_reading INTEGER;

ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS consumption NUMERIC(10, 3);

ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS billing_period VARCHAR(7);

ALTER TABLE service_usages
    ADD COLUMN IF NOT EXISTS reading_date DATE;

UPDATE service_usages su
SET room_id = c.room_id
FROM contracts c
WHERE su.contract_id = c.id
  AND su.room_id IS NULL;

UPDATE service_usages
SET billing_period = LPAD(billing_year::TEXT, 4, '0') || '-' || LPAD(billing_month::TEXT, 2, '0')
WHERE billing_period IS NULL
  AND billing_year IS NOT NULL
  AND billing_month IS NOT NULL;

UPDATE service_usages
SET consumption = (current_reading - previous_reading)::NUMERIC(10, 3)
WHERE consumption IS NULL
  AND previous_reading IS NOT NULL
  AND current_reading IS NOT NULL;

CREATE INDEX IF NOT EXISTS idx_service_usages_room_id
    ON service_usages(room_id);

CREATE INDEX IF NOT EXISTS idx_service_usages_billing_period
    ON service_usages(billing_period);

-- invoice_items
CREATE TABLE IF NOT EXISTS invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(255) NOT NULL,
    unit_price NUMERIC(15, 2) NOT NULL,
    quantity NUMERIC(10, 3) NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    billing_type INTEGER NOT NULL,
    service_usage_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_items_invoice
        FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    CONSTRAINT fk_invoice_items_service_usage
        FOREIGN KEY (service_usage_id) REFERENCES service_usages(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id
    ON invoice_items(invoice_id);

CREATE INDEX IF NOT EXISTS idx_invoice_items_service_usage_id
    ON invoice_items(service_usage_id);

-- notifications
CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    ref_entity_type VARCHAR(50),
    ref_entity_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notifications_user_id
    ON notifications(user_id);

CREATE INDEX IF NOT EXISTS idx_notifications_is_read
    ON notifications(is_read);

CREATE INDEX IF NOT EXISTS idx_notifications_ref
    ON notifications(ref_entity_type, ref_entity_id);

-- tenant_group_members cleanup
ALTER TABLE tenant_group_members
    ADD COLUMN IF NOT EXISTS member_role INTEGER;

ALTER TABLE tenant_group_members
    ADD COLUMN IF NOT EXISTS left_at DATE;

ALTER TABLE tenant_group_members
    ADD COLUMN IF NOT EXISTS id_card_number VARCHAR(50);

ALTER TABLE tenant_group_members
    ADD COLUMN IF NOT EXISTS id_card_front VARCHAR(500);

ALTER TABLE tenant_group_members
    ADD COLUMN IF NOT EXISTS id_card_back VARCHAR(500);

UPDATE tenant_group_members
SET member_role = 2
WHERE member_role IS NULL;

ALTER TABLE tenant_group_members
    ALTER COLUMN member_role SET DEFAULT 2;

ALTER TABLE tenant_group_members
    DROP COLUMN IF EXISTS role;

ALTER TABLE tenant_group_members
    ALTER COLUMN joined_at TYPE DATE
    USING joined_at::DATE;

CREATE INDEX IF NOT EXISTS idx_tenant_group_members_group_role
    ON tenant_group_members(tenant_group_id, member_role);

DROP TABLE IF EXISTS tenant_members;

-- finance_reports snapshot-style columns
ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS report_month VARCHAR(7);

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS total_revenue NUMERIC(15, 2);

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS total_paid NUMERIC(15, 2);

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS total_outstanding NUMERIC(15, 2);

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS total_invoices INTEGER;

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS paid_invoices INTEGER;

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS overdue_invoices INTEGER;

ALTER TABLE finance_reports
    ADD COLUMN IF NOT EXISTS generated_at TIMESTAMP;

UPDATE finance_reports
SET total_revenue = 0
WHERE total_revenue IS NULL;

UPDATE finance_reports
SET total_paid = 0
WHERE total_paid IS NULL;

UPDATE finance_reports
SET total_outstanding = 0
WHERE total_outstanding IS NULL;

UPDATE finance_reports
SET total_invoices = 0
WHERE total_invoices IS NULL;

UPDATE finance_reports
SET paid_invoices = 0
WHERE paid_invoices IS NULL;

UPDATE finance_reports
SET overdue_invoices = 0
WHERE overdue_invoices IS NULL;

UPDATE finance_reports
SET report_month = TO_CHAR(COALESCE(created_at, CURRENT_TIMESTAMP), 'YYYY-MM')
WHERE report_month IS NULL;

UPDATE finance_reports
SET generated_at = COALESCE(generated_at, CURRENT_TIMESTAMP)
WHERE generated_at IS NULL;

CREATE INDEX IF NOT EXISTS idx_finance_reports_report_month
    ON finance_reports(report_month);
