-- =====================================================
-- MIGRATION V2: Expand User Table + 5 New Modules
-- =====================================================
-- This migration:
-- 1. Extends users table with Tenant fields
-- 2. Creates infrastructure module (Room, Asset, Camera)
-- 3. Creates service module (Service, ServiceUsage)
-- 4. Creates finance module (Invoice, Payment, Transaction, FinanceReport)
-- 5. Creates contract module (Contract, ContractTerm, ContractFile)
-- 6. Creates customer module (TenantGroup)

-- =====================================================
-- STEP 1: Expand existing users table
-- =====================================================
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS id_card VARCHAR(50);
ALTER TABLE users ADD COLUMN IF NOT EXISTS address VARCHAR(500);
ALTER TABLE users ADD COLUMN IF NOT EXISTS relationship_to_owner VARCHAR(50) DEFAULT 'TENANT'; -- OWNER, MANAGER, TENANT, GUEST
ALTER TABLE users ADD COLUMN IF NOT EXISTS subscription_id BIGINT;

-- Add foreign key constraint for subscription_id
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_users_subscription'
    ) THEN
        ALTER TABLE users ADD CONSTRAINT fk_users_subscription
            FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE SET NULL;
    END IF;
END$$;

-- Create index for common queries
CREATE INDEX IF NOT EXISTS idx_users_subscription_id ON users(subscription_id);
CREATE INDEX IF NOT EXISTS idx_users_relationship ON users(relationship_to_owner);

-- =====================================================
-- MODULE 2: INFRASTRUCTURE (Cơ sở vật chất)
-- =====================================================

-- TABLE: rooms
CREATE TABLE IF NOT EXISTS rooms (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    floor INTEGER NOT NULL,
    capacity INTEGER NOT NULL DEFAULT 1,
    rent_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1, -- 1=AVAILABLE, 2=OCCUPIED, 3=MAINTENANCE, 4=INACTIVE
    occupied_by_user_id BIGINT, -- NULL if not occupied
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (occupied_by_user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE (subscription_id, code)
);

CREATE INDEX IF NOT EXISTS idx_rooms_subscription ON rooms(subscription_id);
CREATE INDEX IF NOT EXISTS idx_rooms_occupied_by ON rooms(occupied_by_user_id);
CREATE INDEX IF NOT EXISTS idx_rooms_status ON rooms(status);

-- TABLE: assets
CREATE TABLE IF NOT EXISTS assets (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(100) NOT NULL, -- FURNITURE, EQUIPMENT, FIXTURE, OTHER
    quantity INTEGER NOT NULL DEFAULT 1,
    purchase_date DATE,
    value DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1, -- 1=ACTIVE, 2=DAMAGED, 3=MAINTENANCE, 4=DISPOSED
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_assets_room ON assets(room_id);
CREATE INDEX IF NOT EXISTS idx_assets_subscription ON assets(subscription_id);
CREATE INDEX IF NOT EXISTS idx_assets_type ON assets(type);

-- TABLE: asset_maintenance
CREATE TABLE IF NOT EXISTS asset_maintenance (
    id BIGSERIAL PRIMARY KEY,
    asset_id BIGINT NOT NULL,
    maintenance_date DATE NOT NULL,
    description TEXT,
    cost DECIMAL(15, 2) DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1, -- 1=SCHEDULED, 2=IN_PROGRESS, 3=COMPLETED
    next_maintenance_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (asset_id) REFERENCES assets(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_asset_maintenance_asset ON asset_maintenance(asset_id);
CREATE INDEX IF NOT EXISTS idx_asset_maintenance_date ON asset_maintenance(maintenance_date);

-- TABLE: cameras
CREATE TABLE IF NOT EXISTS cameras (
    id BIGSERIAL PRIMARY KEY,
    room_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    ip_address VARCHAR(50) NOT NULL,
    rtsp_url VARCHAR(500),
    status INTEGER NOT NULL DEFAULT 1, -- 1=ACTIVE, 2=OFFLINE, 3=MAINTENANCE, 4=INACTIVE
    location VARCHAR(255),
    installation_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    UNIQUE (subscription_id, code),
    UNIQUE (ip_address)
);

CREATE INDEX IF NOT EXISTS idx_cameras_room ON cameras(room_id);
CREATE INDEX IF NOT EXISTS idx_cameras_subscription ON cameras(subscription_id);
CREATE INDEX IF NOT EXISTS idx_cameras_ip ON cameras(ip_address);

-- =====================================================
-- MODULE 3: SERVICES (Dịch vụ)
-- =====================================================

-- TABLE: services
CREATE TABLE IF NOT EXISTS services (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- WIFI, ELECTRIC, WATER, CLEANING, OTHER
    unit VARCHAR(50) NOT NULL, -- kWh, m3, times, etc.
    base_price DECIMAL(15, 2) NOT NULL DEFAULT 0,
    status INTEGER NOT NULL DEFAULT 1, -- 1=ACTIVE, 2=INACTIVE
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    UNIQUE (subscription_id, code)
);

CREATE INDEX IF NOT EXISTS idx_services_subscription ON services(subscription_id);
CREATE INDEX IF NOT EXISTS idx_services_type ON services(type);

-- TABLE: service_usage
CREATE TABLE IF NOT EXISTS service_usage (
    id BIGSERIAL PRIMARY KEY,
    service_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    usage_quantity DECIMAL(15, 2) NOT NULL,
    unit_price DECIMAL(15, 2) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL, -- usage_quantity * unit_price
    status INTEGER NOT NULL DEFAULT 1, -- 1=RECORDED, 2=INVOICED, 3=PAID
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_service_usage_service ON service_usage(service_id);
CREATE INDEX IF NOT EXISTS idx_service_usage_room ON service_usage(room_id);
CREATE INDEX IF NOT EXISTS idx_service_usage_user ON service_usage(user_id);
CREATE INDEX IF NOT EXISTS idx_service_usage_period ON service_usage(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_service_usage_status ON service_usage(status);

-- =====================================================
-- MODULE 4: FINANCE (Tài chính)
-- =====================================================

-- TABLE: invoices
CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- Tenant
    room_id BIGINT NOT NULL,
    contract_id BIGINT, -- Can be NULL for other invoices
    invoice_number VARCHAR(100) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    issue_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal_rent DECIMAL(15, 2) DEFAULT 0,
    subtotal_services DECIMAL(15, 2) DEFAULT 0,
    total_amount DECIMAL(15, 2) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1, -- 1=DRAFT, 2=ISSUED, 3=PARTIALLY_PAID, 4=PAID, 5=OVERDUE, 6=CANCELLED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    UNIQUE (subscription_id, invoice_number)
);

CREATE INDEX IF NOT EXISTS idx_invoices_subscription ON invoices(subscription_id);
CREATE INDEX IF NOT EXISTS idx_invoices_user ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoices_room ON invoices(room_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_period ON invoices(period_start, period_end);

-- TABLE: payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    subscription_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- Tenant
    payment_date DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    method VARCHAR(50) NOT NULL, -- CASH, BANK_TRANSFER, ONLINE, CHEQUE, OTHER
    transaction_ref VARCHAR(255), -- Reference from bank/payment provider
    status INTEGER NOT NULL DEFAULT 1, -- 1=PENDING, 2=COMPLETED, 3=FAILED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_payments_invoice ON payments(invoice_id);
CREATE INDEX IF NOT EXISTS idx_payments_subscription ON payments(subscription_id);
CREATE INDEX IF NOT EXISTS idx_payments_user ON payments(user_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_date ON payments(payment_date);

-- TABLE: transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    payment_id BIGINT, -- Can be NULL for non-payment transactions
    transaction_date DATE NOT NULL,
    type VARCHAR(50) NOT NULL, -- INCOME, EXPENSE
    amount DECIMAL(15, 2) NOT NULL,
    category VARCHAR(100) NOT NULL, -- RENT, SERVICE, MAINTENANCE, DEPOSIT, REFUND, OTHER
    description TEXT,
    reference_number VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES payments(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_transactions_subscription ON transactions(subscription_id);
CREATE INDEX IF NOT EXISTS idx_transactions_payment ON transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date);
CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions(type);
CREATE INDEX IF NOT EXISTS idx_transactions_category ON transactions(category);

-- TABLE: finance_reports
CREATE TABLE IF NOT EXISTS finance_reports (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL, -- MONTHLY, QUARTERLY, YEARLY, CUSTOM
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    total_income DECIMAL(15, 2) DEFAULT 0,
    total_expense DECIMAL(15, 2) DEFAULT 0,
    profit DECIMAL(15, 2) DEFAULT 0,
    report_date DATE NOT NULL,
    generated_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (generated_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_finance_reports_subscription ON finance_reports(subscription_id);
CREATE INDEX IF NOT EXISTS idx_finance_reports_period ON finance_reports(period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_finance_reports_type ON finance_reports(report_type);

-- =====================================================
-- MODULE 5: CONTRACTS (Hợp đồng)
-- =====================================================

-- TABLE: contracts
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL, -- Tenant
    contract_number VARCHAR(100) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    rent_amount DECIMAL(15, 2) NOT NULL,
    status INTEGER NOT NULL DEFAULT 1, -- 1=DRAFT, 2=ACTIVE, 3=EXPIRED, 4=TERMINATED, 5=RENEWED
    created_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE RESTRICT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    UNIQUE (subscription_id, contract_number)
);

CREATE INDEX IF NOT EXISTS idx_contracts_subscription ON contracts(subscription_id);
CREATE INDEX IF NOT EXISTS idx_contracts_room ON contracts(room_id);
CREATE INDEX IF NOT EXISTS idx_contracts_user ON contracts(user_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contracts_date ON contracts(start_date, end_date);

-- TABLE: contract_terms
CREATE TABLE IF NOT EXISTS contract_terms (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    term_type VARCHAR(100) NOT NULL, -- PAYMENT, DEPOSIT, UTILITIES, MAINTENANCE, PENALTY, OTHER
    description TEXT,
    amount DECIMAL(15, 2),
    is_required BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_contract_terms_contract ON contract_terms(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_terms_type ON contract_terms(term_type);

-- TABLE: contract_files
CREATE TABLE IF NOT EXISTS contract_files (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(100), -- PDF, DOCX, IMAGE, OTHER
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_contract_files_contract ON contract_files(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_files_uploaded_by ON contract_files(uploaded_by_user_id);

-- =====================================================
-- MODULE 1: CUSTOMERS (Khách hàng)
-- =====================================================

-- TABLE: tenant_groups
CREATE TABLE IF NOT EXISTS tenant_groups (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    leader_user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (subscription_id) REFERENCES subscriptions(id) ON DELETE CASCADE,
    FOREIGN KEY (leader_user_id) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE (subscription_id, name)
);

CREATE INDEX IF NOT EXISTS idx_tenant_groups_subscription ON tenant_groups(subscription_id);
CREATE INDEX IF NOT EXISTS idx_tenant_groups_leader ON tenant_groups(leader_user_id);

-- TABLE: tenant_group_members (link users to groups)
CREATE TABLE IF NOT EXISTS tenant_group_members (
    id BIGSERIAL PRIMARY KEY,
    tenant_group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) DEFAULT 'MEMBER', -- LEADER, MEMBER
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (tenant_group_id) REFERENCES tenant_groups(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (tenant_group_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_tenant_group_members_group ON tenant_group_members(tenant_group_id);
CREATE INDEX IF NOT EXISTS idx_tenant_group_members_user ON tenant_group_members(user_id);

-- =====================================================
-- JUNCTION TABLE: invoice_service_usage
-- =====================================================
CREATE TABLE IF NOT EXISTS invoice_service_usage (
    invoice_id BIGINT NOT NULL,
    service_usage_id BIGINT NOT NULL,
    
    PRIMARY KEY (invoice_id, service_usage_id),
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE CASCADE,
    FOREIGN KEY (service_usage_id) REFERENCES service_usage(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_invoice_service_usage_invoice ON invoice_service_usage(invoice_id);
CREATE INDEX IF NOT EXISTS idx_invoice_service_usage_service ON invoice_service_usage(service_usage_id);

-- =====================================================
-- END OF MIGRATION
-- =====================================================
