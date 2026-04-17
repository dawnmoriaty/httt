# 🏢 ERD - Hệ Thống Quản Lý Chung Cư Mini (HTTT)

## 📊 Entity Relationship Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                      RBAC FOUNDATION (Existing)                               │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │  User (id, username, email, password_hash, full_name, status,             │ │
│  │       phone, id_card, address, relationship_to_owner, subscription_id)    │ │
│  │  └─→ 1:N → Subscription (User có thể là Manager của nhiều chung cư)        │ │
│  │  └─→ N:N → Role (via user_role table)                                      │ │
│  │  └─→ 1:N → Room (occupied_by) [Tenant là User khi được gán vào room]      │ │
│  │  └─→ 1:N → Contract (tenant hoặc owner)                                    │ │
│  │  └─→ 1:N → TenantGroup (leader hoặc member)                                │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  ┌────────────────────────────────────────────────────────────────────────────┐ │
│  │  Subscription (id, owner_user_id, title, description, ...)                │ │
│  └────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────────┐
│                 MODULE 1: KHÁCH HÀNG (Customer Management)                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Tenant (id, user_id*, subscription_id*, name, phone, email,             │  │
│  │         id_card, address, status, relationship_to_owner)                │  │
│  │ ├─→ N:1 → User (manager/owner)                                          │  │
│  │ ├─→ N:1 → Subscription (building)                                       │  │
│  │ ├─→ 1:N → Room (occupied_by)                                            │  │
│  │ ├─→ N:1 → TenantGroup (member)                                          │  │
│  │ └─→ 1:N → Contract                                                      │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ TenantGroup (id, subscription_id*, name, description, leader_tenant_id*)│  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → Tenant (leader)                                               │  │
│  │ └─→ 1:N → Tenant (members)                                              │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────────┐
│              MODULE 2: CƠ SỞ VẬT CHẤT (Infrastructure/Asset)                   │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Room (id, subscription_id*, code, floor, capacity,                      │  │
│  │       rent_price, status, occupied_by_user_id*, created_at)             │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → User (occupied_by) [nullable - User này là Tenant]            │  │
│  │ ├─→ 1:N → Asset (contains)                                              │  │
│  │ ├─→ 1:N → Camera (monitored_by)                                         │  │
│  │ └─→ 1:N → Contract (for_room)                                           │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Asset (id, room_id*, subscription_id*, name, type,                      │  │
│  │        quantity, purchase_date, status, value)                          │  │
│  │ ├─→ N:1 → Room                                                          │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ └─→ 1:N → AssetMaintenance (maintenance_records)                        │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ AssetMaintenance (id, asset_id*, date, description,                     │  │
│  │                   cost, status, next_maintenance_date)                  │  │
│  │ └─→ N:1 → Asset                                                         │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Camera (id, room_id*, subscription_id*, code, ip_address,               │  │
│  │         rtsp_url, status, location, installation_date)                  │  │
│  │ ├─→ N:1 → Room                                                          │  │
│  │ └─→ N:1 → Subscription                                                  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────────┐
│                MODULE 3: DỊCH VỤ (Service Management)                          │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Service (id, subscription_id*, code, name, type [WIFI/ELECTRIC/WATER/   │  │
│  │          CLEANING], unit, base_price, status, description)              │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ └─→ 1:N → ServiceUsage (service_usage_records)                          │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ ServiceUsage (id, service_id*, room_id*, user_id*, period_start,        │  │
│  │              period_end, usage_quantity, unit_price, total_amount,      │  │
│  │              status [RECORDED/INVOICED/PAID], created_at)               │  │
│  │ ├─→ N:1 → Service                                                       │  │
│  │ ├─→ N:1 → Room                                                          │  │
│  │ ├─→ N:1 → User (service_user - Tenant sử dụng)                          │  │
│  │ └─→ 1:N → Invoice (included_in)                                         │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────────┐
│                 MODULE 4: TÀI CHÍNH (Finance Management)                       │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Invoice (id, subscription_id*, user_id*, room_id*, contract_id*,        │  │
│  │          invoice_number, period_start, period_end, issue_date,          │  │
│  │          due_date, subtotal_rent, subtotal_services, total_amount,      │  │
│  │          status [DRAFT/ISSUED/PARTIALLY_PAID/PAID/OVERDUE/CANCELLED])  │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → User (tenant)                                                 │  │
│  │ ├─→ N:1 → Room                                                          │  │
│  │ ├─→ N:1 → Contract                                                      │  │
│  │ ├─→ N:M → ServiceUsage (included_services)                              │  │
│  │ └─→ 1:N → Payment (payment_records)                                     │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Payment (id, invoice_id*, subscription_id*, user_id*, payment_date,     │  │
│  │          amount, method [CASH/BANK_TRANSFER/ONLINE], transaction_ref,   │  │
│  │          status [PENDING/COMPLETED/FAILED], notes)                      │  │
│  │ ├─→ N:1 → Invoice                                                       │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → User (tenant)                                                 │  │
│  │ └─→ 1:N → Transaction (transaction_records)                             │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Transaction (id, subscription_id*, payment_id*, transaction_date,       │  │
│  │             type [INCOME/EXPENSE], amount, category [RENT/SERVICE/      │  │
│  │             MAINTENANCE/OTHER], description, reference_number)         │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → Payment                                                       │  │
│  │ └─→ Report data source                                                  │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ FinanceReport (id, subscription_id*, report_type [MONTHLY/QUARTERLY/    │  │
│  │               YEARLY], period_start, period_end, total_income,          │  │
│  │               total_expense, profit, report_date, generated_by_user_id) │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ └─→ N:1 → User (generated_by)                                           │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘


┌─────────────────────────────────────────────────────────────────────────────────┐
│                   MODULE 5: HỢP ĐỒNG (Contract Management)                     │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ Contract (id, subscription_id*, room_id*, user_id*, contract_number,     │  │
│  │           start_date, end_date, rent_amount, status                     │  │
│  │           [DRAFT/ACTIVE/EXPIRED/TERMINATED/RENEWED], created_date)     │  │
│  │ ├─→ N:1 → Subscription                                                  │  │
│  │ ├─→ N:1 → Room                                                          │  │
│  │ ├─→ N:1 → User (tenant)                                                 │  │
│  │ ├─→ 1:N → ContractTerm (contract_terms)                                 │  │
│  │ ├─→ 1:N → Invoice (related_invoices)                                    │  │
│  │ └─→ 1:1 → ContractFile (contract_document)                              │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ ContractTerm (id, contract_id*, term_type [PAYMENT/DEPOSIT/UTILITIES/   │  │
│  │              MAINTENANCE/OTHER], description, amount, required)         │  │
│  │ └─→ N:1 → Contract                                                      │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ ContractFile (id, contract_id*, file_url, file_name, file_type,         │  │
│  │              uploaded_at, uploaded_by_user_id)                          │  │
│  │ ├─→ 1:1 → Contract                                                      │  │
│  │ └─→ N:1 → User (uploaded_by)                                            │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## 📋 CHI TIẾT CÁC ENTITIES

### **MODULE 1: KHÁCH HÀNG**
| Entity | Fields | Relationships | Mô tả |
|--------|--------|---------------|-------|
| **User (Extended)** | id, username, email, password_hash, full_name, status, session_version, **(NEW) phone, id_card, address, relationship_to_owner, subscription_id** | 1:N Subscription, 1:N Room, 1:N ServiceUsage, 1:N Invoice, 1:N Payment, 1:N Contract, 1:N TenantGroup, N:N Role | Người dùng & Tenant (mở rộng User hiện tại với thông tin Tenant) |
| **TenantGroup** | id, subscription_id, name, description, leader_user_id, created_at, updated_at | 1:N Subscription, 1:N User | Nhóm người thuê |

### **MODULE 2: CƠ SỞ VẬT CHẤT**
| Entity | Fields | Relationships | Mô tả |
|--------|--------|---------------|-------|
| **Room** | id, subscription_id, code, floor, capacity, rent_price, status, occupied_by_user_id, created_at | 1:N Subscription, 1:N User, 1:N Asset, 1:N Camera, 1:N Contract | Phòng ốc |
| **Asset** | id, room_id, subscription_id, name, type, quantity, purchase_date, value, status | 1:N Room, 1:N Subscription, 1:N AssetMaintenance | Tài sản (nội thất, thiết bị) |
| **AssetMaintenance** | id, asset_id, date, description, cost, status, next_maintenance_date | 1:N Asset | Bảo trì tài sản |
| **Camera** | id, room_id, subscription_id, code, ip_address, rtsp_url, status, location, installation_date | 1:N Room, 1:N Subscription | Camera giám sát |

### **MODULE 3: DỊCH VỤ**
| Entity | Fields | Relationships | Mô tả |
|--------|--------|---------------|-------|
| **Service** | id, subscription_id, code, name, type (WIFI/ELECTRIC/WATER/CLEANING), unit, base_price, status, description | 1:N Subscription, 1:N ServiceUsage | Dịch vụ (wifi, điện, nước, vệ sinh) |
| **ServiceUsage** | id, service_id, room_id, user_id, period_start, period_end, usage_quantity, unit_price, total_amount, status | 1:N Service, 1:N Room, 1:N User, 1:N Invoice | Sử dụng dịch vụ |

### **MODULE 4: TÀI CHÍNH**
| Entity | Fields | Relationships | Mô tả |
|--------|--------|---------------|-------|
| **Invoice** | id, subscription_id, user_id, room_id, contract_id, invoice_number, period_start, period_end, issue_date, due_date, subtotal_rent, subtotal_services, total_amount, status | 1:N Subscription, 1:N User, 1:N Room, 1:N Contract, N:M ServiceUsage, 1:N Payment | Hoá đơn |
| **Payment** | id, invoice_id, subscription_id, user_id, payment_date, amount, method (CASH/BANK_TRANSFER/ONLINE), transaction_ref, status, notes | 1:N Invoice, 1:N Subscription, 1:N User, 1:N Transaction | Thanh toán |
| **Transaction** | id, subscription_id, payment_id, transaction_date, type (INCOME/EXPENSE), amount, category (RENT/SERVICE/MAINTENANCE/OTHER), description, reference_number | 1:N Subscription, 1:N Payment | Giao dịch tài chính |
| **FinanceReport** | id, subscription_id, report_type (MONTHLY/QUARTERLY/YEARLY), period_start, period_end, total_income, total_expense, profit, report_date, generated_by_user_id | 1:N Subscription, 1:N User | Báo cáo tài chính |

### **MODULE 5: HỢP ĐỒNG**
| Entity | Fields | Relationships | Mô tả |
|--------|--------|---------------|-------|
| **Contract** | id, subscription_id, room_id, user_id, contract_number, start_date, end_date, rent_amount, status (DRAFT/ACTIVE/EXPIRED/TERMINATED/RENEWED), created_date | 1:N Subscription, 1:N Room, 1:N User, 1:N ContractTerm, 1:N Invoice, 1:1 ContractFile | Hợp đồng thuê |
| **ContractTerm** | id, contract_id, term_type (PAYMENT/DEPOSIT/UTILITIES/MAINTENANCE/OTHER), description, amount, required | 1:N Contract | Điều khoản hợp đồng |
| **ContractFile** | id, contract_id, file_url, file_name, file_type, uploaded_at, uploaded_by_user_id | 1:1 Contract, 1:N User | Tệp hợp đồng |

---

## 🔗 RELATIONSHIP SUMMARY

### **Key Relationships:**
1. **Subscription (Hub)** - Tất cả 5 module đều link về Subscription
   - 1:N với: Tenant, TenantGroup, Room, Asset, Camera, Service, Invoice, Payment, Transaction, FinanceReport, Contract

2. **Room (Infrastructure Hub)**
   - 1:N Asset, Camera, ServiceUsage, Invoice, Contract
   - N:1 Tenant (occupied_by)

3. **Tenant (Customer Hub)**
   - 1:N ServiceUsage, Invoice, Payment, Transaction, Contract
   - N:1 TenantGroup

4. **Contract (Business Hub)**
   - 1:N ContractTerm, Invoice
   - N:1 Room, Tenant, Subscription

5. **Invoice (Finance Hub)**
   - 1:N Payment, ServiceUsage
   - N:1 Subscription, Tenant, Room, Contract

---

## 📊 DATABASE SCHEMA FEATURES

✓ **Multi-tenancy**: Mỗi Subscription = 1 chung cư mini
✓ **Audit Trail**: Tất cả entity có created_at, updated_at
✓ **Status Tracking**: Mỗi entity có status để theo dõi trạng thái
✓ **Financial Flow**: Invoice → Payment → Transaction
✓ **Contract Lifecycle**: DRAFT → ACTIVE → EXPIRED/TERMINATED
✓ **Service Usage**: Track chi tiết dịch vụ sử dụng theo phòng, theo tenant

---

## 🎯 CHỨC NĂNG TRÊN MỖI MODULE

### **1. Khách Hàng**
- CRUD Tenant, Tenant Groups
- Quản lý thông tin liên hệ, id_card
- Phân loại mối quan hệ (chủ hộ, thành viên, khách)

### **2. Cơ Sở Vật Chất**
- CRUD Room (floor, capacity, rent_price)
- CRUD Asset & Maintenance records
- CRUD Camera & giám sát video

### **3. Dịch Vụ**
- CRUD Service (WIFI, ELECTRIC, WATER, CLEANING)
- Track ServiceUsage (kỳ, dùng bao nhiêu)
- Tính tiền tự động dựa trên usage

### **4. Tài Chính**
- Tạo Invoice tự động (rent + services)
- Track Payment status (PENDING/COMPLETED)
- Transaction ledger (INCOME/EXPENSE)
- Generate Financial Report (MONTHLY/QUARTERLY/YEARLY)

### **5. Hợp Đồng**
- CRUD Contract (start_date, end_date, rent_amount)
- Quản lý Contract Terms (deposit, utilities)
- Upload Contract File
- Track Contract Status lifecycle

---

## ✅ READY FOR IMPLEMENTATION

Bạn có thể bây giờ:
1. **Triển khai Backend**: Tạo Entities → Repositories → Services → Controllers
2. **Triển khai Frontend**: Tạo pages & components cho từng module
3. **Tạo seed.sql**: Khởi tạo data mẫu

