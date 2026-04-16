"use client";

import { Card } from "@/components/ui/card";
import { useAuth } from "@/lib/auth-context";

export default function DashboardPage() {
  const { user } = useAuth();

  return (
    <div className="space-y-5">
      <Card className="bg-[linear-gradient(135deg,#0b5f95_0%,#0e7abf_45%,#29a7d8_100%)] text-white">
        <p className="text-xs uppercase tracking-[0.22em] text-sky-100">Welcome back</p>
        <h1 className="mt-2 text-2xl font-bold sm:text-3xl">Xin chao, {user?.fullName ?? "Admin"}</h1>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-sky-100">
          Day la dashboard tong quan cho he thong phan quyen RBAC. Ban co the quan ly role, permission va user theo
          cau truc dong.
        </p>
      </Card>

      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        <Card>
          <p className="text-sm text-[var(--muted)]">Role dang chon</p>
          <p className="mt-2 text-xl font-bold">{user?.selectedRoleCode ?? "-"}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Tong role cua user</p>
          <p className="mt-2 text-xl font-bold">{user?.roleCodes.length ?? 0}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Permission active</p>
          <p className="mt-2 text-xl font-bold">{user?.permissions.length ?? 0}</p>
        </Card>
        <Card>
          <p className="text-sm text-[var(--muted)]">Tai khoan</p>
          <p className="mt-2 text-xl font-bold">{user?.username ?? "-"}</p>
        </Card>
      </div>
    </div>
  );
}
