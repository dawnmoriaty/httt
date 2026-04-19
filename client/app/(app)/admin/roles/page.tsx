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
import { apiRequest, ApiClientError, buildPagingQuery } from "@/lib/api-client";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Permission, Role } from "@/lib/types";

type RoleForm = {
  code: string;
  name: string;
  description: string;
};

type ModuleGrantForm = {
  moduleCode: string;
  moduleName: string;
  resourceCode: string;
  resourceName: string;
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
  const [size, setSize] = useState(10);
  const [query, setQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [rolePage, setRolePage] = useState<PageData<Role> | null>(null);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [selectedRole, setSelectedRole] = useState<Role | null>(null);
  const [selectedPermissionIds, setSelectedPermissionIds] = useState<number[]>([]);
  const [form, setForm] = useState<RoleForm>({ code: "", name: "", description: "" });
  const [moduleGrantForm, setModuleGrantForm] = useState<ModuleGrantForm>({
    moduleCode: "",
    moduleName: "",
    resourceCode: "",
    resourceName: "",
  });
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

  const loadRoles = async (targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<Role>>(`/admin/roles?${queryString}`);
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
        await Promise.all([loadRoles(page, size, query), loadPermissions()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải dữ liệu vai trò.");
          pushToast("Không thể tải dữ liệu role.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page, size, query]);

  const onSubmitSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setPage(0);
    setQuery(searchInput);
  };

  const onClearSearch = () => {
    setSearchInput("");
    setQuery("");
    setPage(0);
  };

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
      await loadRoles(0, size, query);
      setPage(0);
      setMessage("Tạo vai trò thành công.");
      pushToast("Tạo role thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tạo vai trò thất bại.");
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
      await loadRoles(page, size, query);
      setMessage("Cập nhật quyền cho vai trò thành công.");
      pushToast("Cập nhật quyền cho role thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Cập nhật quyền thất bại.");
        pushToast("Cập nhật quyền thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onGrantModule = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selectedRole) {
      setError("Hay chon role de cap module.");
      pushToast("Hãy chọn role để cấp module.", "error");
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest(`/admin/tenant-access/roles/${selectedRole.id}/grant-module`, {
        method: "POST",
        body: {
          moduleCode: moduleGrantForm.moduleCode,
          moduleName: moduleGrantForm.moduleName,
          resourceCode: moduleGrantForm.resourceCode,
          resourceName: moduleGrantForm.resourceName,
          actions: [
            { actionCode: "VIEW", actionName: "Xem" },
            { actionCode: "ADD", actionName: "Them" },
            { actionCode: "UPDATE", actionName: "Sua" },
            { actionCode: "DELETE", actionName: "Xóa" },
          ],
        },
      });

      setModuleGrantForm({
        moduleCode: "",
        moduleName: "",
        resourceCode: "",
        resourceName: "",
      });
      await Promise.all([loadRoles(page, size, query), loadPermissions()]);
      setMessage("Cấp module cho vai trò thành công.");
      pushToast("Cấp module cho role thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Cấp module thất bại.");
        pushToast("Cấp module thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Danh sách vai trò</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Tạo vai trò mới và phân quyền theo ma trận module / resource / action.</p>
      </div>

      <Card>
        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitSearch}>
          <Field label="Tìm kiếm vai trò">
            <TextInput
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Nhập mã, tên hoặc mô tả"
              className="min-w-72"
            />
          </Field>
          <div className="flex gap-2">
            <Button type="submit" variant="secondary">
              Tìm
            </Button>
            <Button type="button" variant="ghost" onClick={onClearSearch}>
              Xóa lọc
            </Button>
          </div>
        </form>
      </Card>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <div className="grid gap-5 xl:grid-cols-[1.1fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sách vai trò</h2>
          <Table
            headers={["ID", "Mã", "Tên vai trò", "Trạng thái", "Thao tác"]}
            rows={(rolePage?.content ?? []).map((role) => [
              role.id,
              <span className="font-mono text-xs">{role.code}</span>,
              role.name,
              <Badge key={`status-${role.id}`} variant={role.status === 1 ? "success" : "danger"}>
                {toVietnameseStatus(role.status)}
              </Badge>,
              <Button key={`pick-${role.id}`} variant="secondary" onClick={() => onSelectRole(role)}>
                Chọn
              </Button>,
            ])}
          />

          <Pagination
            page={rolePage?.number ?? 0}
            totalPages={Math.max(rolePage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
            size={size}
            onSizeChange={(nextSize) => {
              setSize(nextSize);
              setPage(0);
            }}
          />
        </Card>

        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Tạo vai trò mới</h2>
          <form className="space-y-3" onSubmit={onCreateRole}>
            <Field label="Mã vai trò">
              <TextInput value={form.code} onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))} required />
            </Field>
            <Field label="Tên vai trò">
              <TextInput value={form.name} onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))} required />
            </Field>
            <Field label="Mô tả">
              <TextAreaInput
                rows={3}
                value={form.description}
                onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
              />
            </Field>
            <Button type="submit" loading={saving}>
              Tạo vai trò
            </Button>
          </form>
        </Card>
      </div>

      <Card className="space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="text-lg font-semibold">Ma trận phân quyền</h2>
          <div className="text-sm text-[var(--muted)]">
            Vai trò đang chọn: <span className="font-semibold text-[var(--foreground)]">{selectedRole?.name ?? "Chưa chọn"}</span>
          </div>
        </div>

        <div className="overflow-x-auto rounded-xl border border-[var(--border)]">
          <table className="min-w-full bg-white text-sm">
            <thead className="bg-[var(--surface-muted)]">
              <tr>
                <th className="px-4 py-3 text-left font-semibold">Module / Resource</th>
                <th className="px-4 py-3 text-left font-semibold">Hành động</th>
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
          Lưu phân quyền
        </Button>
      </Card>

      <Card className="space-y-4">
        <h2 className="text-lg font-semibold">Cấp module mới cho vai trò</h2>
        <p className="text-sm text-[var(--muted)]">Thêm nhanh quyền cho phân hệ mới, sau đó có thể tinh chỉnh trong ma trận.</p>
        <p className="text-sm text-[var(--muted)]">
          Vai trò đang chọn: <span className="font-semibold text-[var(--foreground)]">{selectedRole?.name ?? "Chưa chọn"}</span>
        </p>
        <form className="grid gap-3 md:grid-cols-4" onSubmit={onGrantModule}>
          <Field label="Mã module">
            <TextInput
              value={moduleGrantForm.moduleCode}
              onChange={(event) => setModuleGrantForm((prev) => ({ ...prev, moduleCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Tên module">
            <TextInput
              value={moduleGrantForm.moduleName}
              onChange={(event) => setModuleGrantForm((prev) => ({ ...prev, moduleName: event.target.value }))}
              required
            />
          </Field>
          <Field label="Mã resource">
            <TextInput
              value={moduleGrantForm.resourceCode}
              onChange={(event) => setModuleGrantForm((prev) => ({ ...prev, resourceCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Tên resource">
            <TextInput
              value={moduleGrantForm.resourceName}
              onChange={(event) => setModuleGrantForm((prev) => ({ ...prev, resourceName: event.target.value }))}
              required
            />
          </Field>
          <div className="md:col-span-4">
            <Button type="submit" disabled={!selectedRole} loading={saving}>
              Tạo quyền và gán vào vai trò
            </Button>
          </div>
        </form>
      </Card>
    </div>
  );
}
