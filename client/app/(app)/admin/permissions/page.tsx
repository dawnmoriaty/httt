"use client";

import { useEffect, useMemo, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError } from "@/lib/api-client";
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
  const [permissionPage, setPermissionPage] = useState<PageData<Permission> | null>(null);
  const [query, setQuery] = useState("");
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

  const loadPermissions = async (targetPage: number) => {
    const data = await apiRequest<PageData<Permission>>(`/admin/permissions?page=${targetPage}&size=20`);
    setPermissionPage(data);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await loadPermissions(page);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Khong the tai danh sach permission.");
          pushToast("Không thể tải danh sách quyền.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page]);

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
      setMessage("Tao permission thanh cong.");
      pushToast("Tạo permission thành công.", "success");
      await loadPermissions(page);
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Tao permission that bai.");
        pushToast("Tạo permission thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const rows = useMemo(() => {
    const source = permissionPage?.content ?? [];
    const normalizedQuery = query.trim().toLowerCase();
    const filtered =
      normalizedQuery.length === 0
        ? source
        : source.filter((permission) =>
            [permission.moduleName, permission.resourceName, permission.actionCode]
              .join(" ")
              .toLowerCase()
              .includes(normalizedQuery),
          );

    return filtered.map((permission) => [
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
  }, [permissionPage, query]);

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Danh muc quyen</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">Danh sach permission theo module, resource va action de cap role dong.</p>
      </div>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <Card className="space-y-4">
        <h2 className="text-lg font-semibold">Tao permission moi</h2>
        <form className="grid gap-3 md:grid-cols-3" onSubmit={onCreatePermission}>
          <Field label="Module code">
            <TextInput
              value={form.moduleCode}
              onChange={(event) => setForm((prev) => ({ ...prev, moduleCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Module name">
            <TextInput
              value={form.moduleName}
              onChange={(event) => setForm((prev) => ({ ...prev, moduleName: event.target.value }))}
              required
            />
          </Field>
          <Field label="Resource code">
            <TextInput
              value={form.resourceCode}
              onChange={(event) => setForm((prev) => ({ ...prev, resourceCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Resource name">
            <TextInput
              value={form.resourceName}
              onChange={(event) => setForm((prev) => ({ ...prev, resourceName: event.target.value }))}
              required
            />
          </Field>
          <Field label="Action code">
            <TextInput
              value={form.actionCode}
              onChange={(event) => setForm((prev) => ({ ...prev, actionCode: event.target.value }))}
              required
            />
          </Field>
          <Field label="Action name">
            <TextInput
              value={form.actionName}
              onChange={(event) => setForm((prev) => ({ ...prev, actionName: event.target.value }))}
              required
            />
          </Field>
          <div className="md:col-span-3">
            <Button type="submit" loading={saving}>
              Tao permission
            </Button>
          </div>
        </form>
      </Card>

      <Card className="space-y-4">
        <Field label="Tim nhanh theo module / resource / action">
          <TextInput value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Vi du: subscription VIEW" />
        </Field>

        <Table headers={["ID", "Module", "Resource", "Action", "Status"]} rows={rows} />

        <Pagination
          page={permissionPage?.number ?? 0}
          totalPages={Math.max(permissionPage?.totalPages ?? 0, 1)}
          onPageChange={(nextPage) => setPage(nextPage)}
        />

        {loading ? <p className="text-sm text-[var(--muted)]">Dang tai du lieu...</p> : null}
      </Card>
    </div>
  );
}
