"use client";

import { useEffect, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, SelectInput, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError } from "@/lib/api-client";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Role, User } from "@/lib/types";

type UserForm = {
  username: string;
  fullName: string;
  email: string;
  password: string;
  roleIds: number[];
};

export default function UsersPage() {
  return (
    <AuthGuard requiredPermission={{ resource: "user", action: "VIEW" }}>
      <UsersPageContent />
    </AuthGuard>
  );
}

function UsersPageContent() {
  const [page, setPage] = useState(0);
  const [userPage, setUserPage] = useState<PageData<User> | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [selectedRoleIds, setSelectedRoleIds] = useState<number[]>([]);
  const [form, setForm] = useState<UserForm>({
    username: "",
    fullName: "",
    email: "",
    password: "",
    roleIds: [],
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();

  const loadUsers = async (targetPage: number) => {
    const data = await apiRequest<PageData<User>>(`/admin/users?page=${targetPage}&size=10`);
    setUserPage(data);
  };

  const loadRoles = async () => {
    const data = await apiRequest<PageData<Role>>(`/admin/roles?page=0&size=200`);
    setRoles(data.content);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await Promise.all([loadUsers(page), loadRoles()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Khong the tai du lieu user.");
          pushToast("Không thể tải dữ liệu user.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page]);

  const onCreateUser = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<User>("/admin/users", {
        method: "POST",
        body: form,
      });
      setForm({ username: "", fullName: "", email: "", password: "", roleIds: [] });
      await loadUsers(0);
      setPage(0);
      setMessage("Tao user thanh cong.");
      pushToast("Tạo user thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tao user that bai.");
        pushToast("Tạo user thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onSelectUser = (user: User) => {
    setSelectedUser(user);
    setSelectedRoleIds(user.roles.map((role) => role.id));
  };

  const onSaveUserRoles = async () => {
    if (!selectedUser) {
      setError("Hay chon user de cap nhat role.");
      pushToast("Hãy chọn user để cập nhật role.", "error");
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<User>(`/admin/users/${selectedUser.id}/roles`, {
        method: "PUT",
        body: {
          roleIds: selectedRoleIds,
        },
      });
      await loadUsers(page);
      setMessage("Cap nhat role cho user thanh cong.");
      pushToast("Cập nhật role cho user thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Cap nhat role that bai.");
        pushToast("Cập nhật role thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Quan ly tai khoan</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Tao user moi va gan role dong theo ma tran RBAC.</p>
      </div>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <div className="grid gap-5 xl:grid-cols-[1.15fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sach user</h2>
          <Table
            headers={["ID", "Username", "Ho ten", "Role", "Status", "Action"]}
            rows={(userPage?.content ?? []).map((user) => [
              user.id,
              user.username,
              user.fullName,
              <div key={`roles-${user.id}`} className="flex flex-wrap gap-1.5">
                {user.roles.map((role) => (
                  <Badge key={role.id} variant="info">
                    {role.code}
                  </Badge>
                ))}
              </div>,
              <Badge key={`status-${user.id}`} variant={user.status === 1 ? "success" : "danger"}>
                {toVietnameseStatus(user.status)}
              </Badge>,
              <Button key={`pick-${user.id}`} variant="secondary" onClick={() => onSelectUser(user)}>
                Chon
              </Button>,
            ])}
          />

          <Pagination
            page={userPage?.number ?? 0}
            totalPages={Math.max(userPage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
          />

          {loading ? <p className="text-sm text-[var(--muted)]">Dang tai du lieu...</p> : null}
        </Card>

        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Tao user moi</h2>
          <form className="space-y-3" onSubmit={onCreateUser}>
            <Field label="Username">
              <TextInput
                value={form.username}
                onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
                required
              />
            </Field>
            <Field label="Ho ten">
              <TextInput
                value={form.fullName}
                onChange={(event) => setForm((prev) => ({ ...prev, fullName: event.target.value }))}
                required
              />
            </Field>
            <Field label="Email">
              <TextInput
                type="email"
                value={form.email}
                onChange={(event) => setForm((prev) => ({ ...prev, email: event.target.value }))}
                required
              />
            </Field>
            <Field label="Password">
              <TextInput
                type="password"
                value={form.password}
                onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))}
                required
              />
            </Field>
            <Field label="Role mac dinh">
              <SelectInput
                value={form.roleIds[0] ?? ""}
                onChange={(event) => setForm((prev) => ({ ...prev, roleIds: [Number(event.target.value)] }))}
                required
              >
                <option value="" disabled>
                  Chon role
                </option>
                {roles.map((role) => (
                  <option key={role.id} value={role.id}>
                    {role.name}
                  </option>
                ))}
              </SelectInput>
            </Field>
            <Button type="submit" loading={saving}>
              Tao user
            </Button>
          </form>

          <div className="h-px bg-[var(--border)]" />

          <div className="space-y-3">
            <h3 className="font-semibold">Gan role cho user</h3>
            <p className="text-sm text-[var(--muted)]">User dang chon: {selectedUser?.username ?? "Chua chon"}</p>
            <Field label="Danh sach role">
              <SelectInput
                multiple
                value={selectedRoleIds.map(String)}
                onChange={(event) => {
                  const values = Array.from(event.target.selectedOptions).map((option) => Number(option.value));
                  setSelectedRoleIds(values);
                }}
                className="min-h-32"
              >
                {roles.map((role) => (
                  <option key={role.id} value={role.id}>
                    {role.code} - {role.name}
                  </option>
                ))}
              </SelectInput>
            </Field>
            <Button onClick={onSaveUserRoles} disabled={!selectedUser} loading={saving}>
              Luu role cho user
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
