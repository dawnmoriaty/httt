"use client";

import { useEffect, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError, buildPagingQuery } from "@/lib/api-client";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Permission } from "@/lib/types";

type PermissionForm = {
  moduleCode: string;
  moduleName: string;
  resourceCode: string;
  resourceName: string;
  actionCode: string;
  actionName: string;
};

export default function PermissionsPage() {
  return (
    <AuthGuard requiredPermission={{ resource: "permission", action: "VIEW" }}>
      <PermissionsPageContent />
    </AuthGuard>
  );
}

function PermissionsPageContent() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [permissionPage, setPermissionPage] = useState<PageData<Permission> | null>(null);
  const [query, setQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [form, setForm] = useState<PermissionForm>({
    moduleCode: "",
    moduleName: "",
    resourceCode: "",
    resourceName: "",
    actionCode: "",
    actionName: "",
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();

  const loadPermissions = async (targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<Permission>>(`/admin/permissions?${queryString}`);
    setPermissionPage(data);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await loadPermissions(page, size, query);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải danh sách quyền.");
          pushToast("Không thể tải danh sách quyền.", "error");
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

  const onCreatePermission = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<Permission>("/admin/permissions", {
        method: "POST",
        body: form,
      });
      setForm({
        moduleCode: "",
        moduleName: "",
        resourceCode: "",
        resourceName: "",
        actionCode: "",
        actionName: "",
      });
      setMessage("Tạo quyền thành công.");
      pushToast("Tạo permission thành công.", "success");
      await loadPermissions(page, size, query);
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tạo quyền thất bại.");
        pushToast("Tạo permission thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const rows = (permissionPage?.content ?? []).map((permission) => [
      permission.id,
      permission.moduleName,
      permission.resourceName,
      <span key={`action-${permission.id}`} className="font-mono text-xs">
        {permission.actionCode}
      </span>,
      <Badge key={`status-${permission.id}`} variant={permission.status === 1 ? "success" : "danger"}>
        {toVietnameseStatus(permission.status)}
      </Badge>,
    ]);

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Danh mục quyền</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Quản lý danh sách quyền theo module, resource và action.</p>
      </div>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <Card className="space-y-4">
        <h2 className="text-lg font-semibold">Tạo quyền mới</h2>
        <form className="grid gap-3 md:grid-cols-3" onSubmit={onCreatePermission}>
          <Field label="Mã module">
            <TextInput
              value={form.moduleCode}
              onChange={(event) => setForm((prev) => ({ ...prev, moduleCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Tên module">
            <TextInput
              value={form.moduleName}
              onChange={(event) => setForm((prev) => ({ ...prev, moduleName: event.target.value }))}
              required
            />
          </Field>
          <Field label="Mã resource">
            <TextInput
              value={form.resourceCode}
              onChange={(event) => setForm((prev) => ({ ...prev, resourceCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Tên resource">
            <TextInput
              value={form.resourceName}
              onChange={(event) => setForm((prev) => ({ ...prev, resourceName: event.target.value }))}
              required
            />
          </Field>
          <Field label="Mã action">
            <TextInput
              value={form.actionCode}
              onChange={(event) => setForm((prev) => ({ ...prev, actionCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Tên action">
            <TextInput
              value={form.actionName}
              onChange={(event) => setForm((prev) => ({ ...prev, actionName: event.target.value }))}
              required
            />
          </Field>
          <div className="md:col-span-3">
            <Button type="submit" loading={saving}>
              Tạo quyền
            </Button>
          </div>
        </form>
      </Card>

      <Card className="space-y-4">
        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitSearch}>
          <Field label="Tìm kiếm quyền">
            <TextInput
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Ví dụ: subscription VIEW"
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

        <Table headers={["ID", "Module", "Resource", "Action", "Trạng thái"]} rows={rows} />

        <Pagination
          page={permissionPage?.number ?? 0}
          totalPages={Math.max(permissionPage?.totalPages ?? 0, 1)}
          onPageChange={(nextPage) => setPage(nextPage)}
          size={size}
          onSizeChange={(nextSize) => {
            setSize(nextSize);
            setPage(0);
          }}
        />

        {loading ? <p className="text-sm text-[var(--muted)]">Đang tải dữ liệu...</p> : null}
      </Card>
    </div>
  );
}
