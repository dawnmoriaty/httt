"use client";

import { useEffect, useMemo, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, TextAreaInput, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError } from "@/lib/api-client";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Permission, Role } from "@/lib/types";

type RoleForm = {
  code: string;
  name: string;
  description: string;
};

export default function RolesPage() {
  return (
    <AuthGuard requiredPermission={{ resource: "role", action: "VIEW" }}>
      <RolesPageContent />
    </AuthGuard>
  );
}

function RolesPageContent() {
  const [page, setPage] = useState(0);
  const [rolePage, setRolePage] = useState<PageData<Role> | null>(null);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([]);
  const [form, setForm] = useState<RoleForm>({ code: "", name: "", description: "" });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();
  const [error, setError] = useState<string | null>(null);

  const permissionGroups = useMemo(() => {
    const grouped = new Map<string, Permission[]>();
    for (const permission of permissions) {
      const key = `${permission.moduleName}__${permission.resourceName}`;
      if (!grouped.has(key)) {
        grouped.set(key, []);
      }
      grouped.get(key)?.push(permission);
    }
    return Array.from(grouped.entries()).map(([key, items]) => {
      const [moduleName, resourceName] = key.split("__");
      return {
        moduleName,
        resourceName,
        permissions: items.sort((a, b) => a.actionCode.localeCompare(b.actionCode)),
      };
    });
  }, [permissions]);

  const loadRoles = async (targetPage: number) => {
    const data = await apiRequest<PageData<Role>>(`/admin/roles?page=${targetPage}&size=10`);
    setRolePage(data);
  };

  const loadPermissions = async () => {
    const data = await apiRequest<PageData<Permission>>(`/admin/permissions?page=0&size=300`);
    setPermissions(data.content);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await Promise.all([loadRoles(page), loadPermissions()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Khong the tai du lieu role.");
          pushToast("Không thể tải dữ liệu role.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page]);

  const onSelectRole = (role: Role) => {
    setSelectedRole(role);
    setSelectedPermissionIds(role.permissionIds);
    setForm({ code: role.code, name: role.name, description: role.description ?? "" });
  };

  const onCreateRole = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<Role>("/admin/roles", {
        method: "POST",
        body: {
          code: form.code,
          name: form.name,
          description: form.description,
        },
      });
      setForm({ code: "", name: "", description: "" });
      await loadRoles(0);
      setPage(0);
      setMessage("Tao role thanh cong.");
      pushToast("Tạo role thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tao role that bai.");
        pushToast("Tạo role thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onAssignPermissions = async () => {
    if (!selectedRole) {
      setError("Hay chon role de cap nhat quyen.");
      pushToast("Hãy chọn role để cập nhật quyền.", "error");
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<Role>(`/admin/roles/${selectedRole.id}/permissions`, {
        method: "PUT",
        body: {
          permissionIds: selectedPermissionIds,
        },
      });
      await loadRoles(page);
      setMessage("Cap nhat quyen cho role thanh cong.");
      pushToast("Cập nhật quyền cho role thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Cap nhat quyen that bai.");
        pushToast("Cập nhật quyền thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Danh sach nhom quyen</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Tao role moi va tick permission theo ma tran module/resource/action.</p>
      </div>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <div className="grid gap-5 xl:grid-cols-[1.1fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sach role</h2>
          <Table
            headers={["ID", "Code", "Ten role", "Status", "Action"]}
            rows={(rolePage?.content ?? []).map((role) => [
              role.id,
              <span className="font-mono text-xs">{role.code}</span>,
              role.name,
              <Badge key={`status-${role.id}`} variant={role.status === 1 ? "success" : "danger"}>
                {toVietnameseStatus(role.status)}
              </Badge>,
              <Button key={`pick-${role.id}`} variant="secondary" onClick={() => onSelectRole(role)}>
                Chon
              </Button>,
            ])}
          />

          <Pagination
            page={rolePage?.number ?? 0}
            totalPages={Math.max(rolePage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
          />
        </Card>

        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Tao role moi</h2>
          <form className="space-y-3" onSubmit={onCreateRole}>
            <Field label="Role code">
              <TextInput value={form.code} onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))} required />
            </Field>
            <Field label="Role name">
              <TextInput value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} required />
            </Field>
            <Field label="Description">
              <TextAreaInput
                rows={3}
                value={form.description}
                onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
              />
            </Field>
            <Button type="submit" loading={saving}>
              Tao role
            </Button>
          </form>
        </Card>
      </div>

      <Card className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-lg font-semibold">Permission Matrix</h2>
          <div className="text-sm text-[var(--muted)]">
            Role dang chon: <span className="font-semibold text-[var(--foreground)]">{selectedRole?.name ?? "Chua chon"}</span>
          </div>
        </div>

        <div className="overflow-x-auto rounded-xl border border-[var(--border)]">
          <table className="min-w-full bg-white text-sm">
            <thead className="bg-[var(--surface-muted)]">
              <tr>
                <th className="px-4 py-3 text-left font-semibold">Module / Resource</th>
                <th className="px-4 py-3 text-left font-semibold">Actions</th>
              </tr>
            </thead>
            <tbody>
              {permissionGroups.map((group) => (
                <tr key={`${group.moduleName}-${group.resourceName}`} className="border-t border-[var(--border)]">
                  <td className="px-4 py-3 align-top">
                    <p className="font-semibold text-[var(--foreground)]">{group.moduleName}</p>
                    <p className="text-xs text-[var(--muted)]">{group.resourceName}</p>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-2">
                      {group.permissions.map((permission) => {
                        const checked = selectedPermissionIds.includes(permission.id);
                        return (
                          <label
                            key={permission.id}
                            className="inline-flex cursor-pointer items-center gap-2 rounded-lg border border-[var(--border)] bg-[var(--surface-muted)] px-3 py-1.5 text-xs font-semibold"
                          >
                            <input
                              type="checkbox"
                              checked={checked}
                              onChange={(event) => {
                                setSelectedPermissionIds((prev) => {
                                  if (event.target.checked) {
                                    return Array.from(new Set([...prev, permission.id]));
                                  }
                                  return prev.filter((id) => id !== permission.id);
                                });
                              }}
                              disabled={!selectedRole || loading}
                            />
                            {permission.actionCode}
                          </label>
                        );
                      })}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <Button onClick={onAssignPermissions} disabled={!selectedRole || loading} loading={saving}>
          Luu phan quyen role
        </Button>
      </Card>
    </div>
  );
}
