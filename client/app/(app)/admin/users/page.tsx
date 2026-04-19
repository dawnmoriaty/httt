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
import { apiRequest, ApiClientError, buildPagingQuery } from "@/lib/api-client";
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
  const [size, setSize] = useState(10);
  const [query, setQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
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

  const loadUsers = async (targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<User>>(`/admin/users?${queryString}`);
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
        await Promise.all([loadUsers(page, size, query), loadRoles()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải dữ liệu người dùng.");
          pushToast("Không thể tải dữ liệu user.", "error");
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
      await loadUsers(0, size, query);
      setPage(0);
      setMessage("Tạo người dùng thành công.");
      pushToast("Tạo user thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tạo người dùng thất bại.");
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
      await loadUsers(page, size, query);
      setMessage("Cập nhật vai trò cho người dùng thành công.");
      pushToast("Cập nhật role cho user thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Cập nhật vai trò thất bại.");
        pushToast("Cập nhật role thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Quản lý tài khoản</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Tạo người dùng mới và gán nhiều vai trò theo ma trận RBAC.</p>
      </div>

      <Card>
        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitSearch}>
          <Field label="Tìm kiếm người dùng">
            <TextInput
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Nhập username, họ tên hoặc email"
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

      <div className="grid gap-5 xl:grid-cols-[1.15fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sách người dùng</h2>
          <Table
            headers={["ID", "Tên đăng nhập", "Họ tên", "Vai trò", "Trạng thái", "Thao tác"]}
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
                Chọn
              </Button>,
            ])}
          />

          <Pagination
            page={userPage?.number ?? 0}
            totalPages={Math.max(userPage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
            size={size}
            onSizeChange={(nextSize) => {
              setSize(nextSize);
              setPage(0);
            }}
          />

          {loading ? <p className="text-sm text-[var(--muted)]">Đang tải dữ liệu...</p> : null}
        </Card>

        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Tạo người dùng mới</h2>
          <form className="space-y-3" onSubmit={onCreateUser}>
            <Field label="Tên đăng nhập">
              <TextInput
                value={form.username}
                onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
                required
              />
            </Field>
            <Field label="Họ tên">
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
            <Field label="Vai trò">
              <div className="max-h-44 overflow-auto rounded-xl border border-[var(--border)] bg-white p-3">
                <div className="grid gap-2">
                  {roles.map((role) => {
                    const checked = form.roleIds.includes(role.id);
                    return (
                      <label key={role.id} className="inline-flex items-center gap-2 text-sm">
                        <input
                          type="checkbox"
                          checked={checked}
                          onChange={(event) => {
                            setForm((prev) => {
                              if (event.target.checked) {
                                return { ...prev, roleIds: Array.from(new Set([...prev.roleIds, role.id])) };
                              }
                              return { ...prev, roleIds: prev.roleIds.filter((id) => id !== role.id) };
                            });
                          }}
                        />
                        <span>
                          {role.code} - {role.name}
                        </span>
                      </label>
                    );
                  })}
                </div>
              </div>
              {form.roleIds.length === 0 ? <span className="text-xs text-[var(--danger)]">Cần chọn ít nhất 1 vai trò.</span> : null}
            </Field>
            <Button type="submit" loading={saving} disabled={form.roleIds.length === 0}>
              Tạo người dùng
            </Button>
          </form>

          <div className="h-px bg-[var(--border)]" />

          <div className="space-y-3">
            <h3 className="font-semibold">Gán vai trò cho người dùng</h3>
            <p className="text-sm text-[var(--muted)]">Người dùng đang chọn: {selectedUser?.username ?? "Chưa chọn"}</p>
            <Field label="Danh sách vai trò">
              <div className="max-h-52 overflow-auto rounded-xl border border-[var(--border)] bg-white p-3">
                <div className="grid gap-2">
                  {roles.map((role) => {
                    const checked = selectedRoleIds.includes(role.id);
                    return (
                      <label key={role.id} className="inline-flex items-center gap-2 text-sm">
                        <input
                          type="checkbox"
                          checked={checked}
                          onChange={(event) => {
                            setSelectedRoleIds((prev) => {
                              if (event.target.checked) {
                                return Array.from(new Set([...prev, role.id]));
                              }
                              return prev.filter((id) => id !== role.id);
                            });
                          }}
                        />
                        <span>
                          {role.code} - {role.name}
                        </span>
                      </label>
                    );
                  })}
                </div>
              </div>
            </Field>
            <Button onClick={onSaveUserRoles} disabled={!selectedUser || selectedRoleIds.length === 0} loading={saving}>
              Lưu vai trò
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
