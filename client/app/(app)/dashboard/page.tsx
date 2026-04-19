"use client";

import { Card } from "@/components/ui/card";
import { useAuth } from "@/lib/auth-context";

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-5">
      <Card className="bg-[linear-gradient(135deg,#0b5f95_0%,#0e7abf_45%,#29a7d8_100%)] text-white">
        <p className="text-xs uppercase tracking-[0.22em] text-sky-100">Welcome back</p>
        <h1 className="mt-2 text-2xl font-bold sm:text-3xl">Xin chào, {user?.fullName ?? "Admin"}</h1>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-sky-100">
          Đây là dashboard tổng quan cho hệ thống phân quyền RBAC và nghiệp vụ quản lý người thuê. Bạn có thể quản lý
          role, permission, user và tenant group theo cấu trúc module động.
        </p>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <Card>
          <p className="text-sm text-[var(--muted)]">Vai trò đang chọn</p>
          <p className="mt-2 text-xl font-bold">{user?.selectedRoleCode ?? "-"}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Tổng vai trò của user</p>
          <p className="mt-2 text-xl font-bold">{user?.roleCodes.length ?? 0}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Permission đang hoạt động</p>
          <p className="mt-2 text-xl font-bold">{user?.permissions.length ?? 0}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Tài khoản</p>
          <p className="mt-2 text-xl font-bold">{user?.username ?? "-"}</p>
        </Card>
      </div>

      <Card className="space-y-2">
        <h2 className="text-lg font-semibold">Ghi chú phân quyền nâng cao</h2>
        <p className="text-sm text-[var(--muted)] leading-7">
          Hệ thống hiện có cả RBAC theo permission và ABAC ownership theo dữ liệu. Với module Subscription và Tenant Group,
          user thường chỉ có thể thao tác bản ghi thuộc phạm vi quản lý của mình. SUPER_ADMIN được xem và thao tác toàn bộ dữ liệu.
        </p>
      </Card>
    </div>
  );
}
