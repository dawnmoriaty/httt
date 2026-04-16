"use client";

import { useEffect, useMemo, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { Field, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError } from "@/lib/api-client";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Permission } from "@/lib/types";

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
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
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
