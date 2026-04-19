"use client";

import { useEffect, useState } from "react";
import { AuthGuard } from "@/components/auth/auth-guard";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, SelectInput, TextAreaInput, TextInput } from "@/components/ui/field";
import { Pagination } from "@/components/ui/pagination";
import { Table } from "@/components/ui/table";
import { useToast } from "@/components/ui/toast-provider";
import { apiRequest, ApiClientError, buildPagingQuery } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { toVietnameseStatus } from "@/lib/format";
import type { PageData, Subscription } from "@/lib/types";

type SubscriptionForm = {
  title: string;
  description: string;
  status: number;
};

export default function SubscriptionsPage() {
  return (
    <AuthGuard requiredPermission={{ resource: "subscription", action: "VIEW" }}>
      <SubscriptionsPageContent />
    </AuthGuard>
  );
}

function SubscriptionsPageContent() {
  const { hasPermission, user } = useAuth();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [query, setQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [subscriptionPage, setSubscriptionPage] = useState<PageData<Subscription> | null>(null);
  const [selected, setSelected] = useState<Subscription | null>(null);
  const [form, setForm] = useState<SubscriptionForm>({ title: "", description: "", status: 1 });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();

  const canAdd = hasPermission("subscription", "ADD");
  const canUpdate = hasPermission("subscription", "UPDATE");
  const canDelete = hasPermission("subscription", "DELETE");
  const isSuperAdmin = user?.roleCodes?.includes("SUPER_ADMIN") ?? false;

  const loadSubscriptions = async (targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<Subscription>>(`/subscriptions?${queryString}`);
    setSubscriptionPage(data);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await loadSubscriptions(page, size, query);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải danh sách subscription.");
          pushToast("Không thể tải danh sách subscription.", "error");
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

  const onPick = (item: Subscription) => {
    setSelected(item);
    setForm({
      title: item.title,
      description: item.description ?? "",
      status: item.status,
    });
  };

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      if (selected) {
        await apiRequest<Subscription>(`/subscriptions/${selected.id}`, {
          method: "PUT",
          body: form,
        });
        setMessage("Cập nhật subscription thành công.");
        pushToast("Cập nhật subscription thành công.", "success");
      } else {
        await apiRequest<Subscription>("/subscriptions", {
          method: "POST",
          body: form,
        });
        setMessage("Tạo subscription thành công.");
        pushToast("Tạo subscription thành công.", "success");
      }

      setSelected(null);
      setForm({ title: "", description: "", status: 1 });
      await loadSubscriptions(page, size, query);
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Lưu subscription thất bại.");
        pushToast("Lưu subscription thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onDelete = async (item: Subscription) => {
    if (!window.confirm(`Xóa subscription ${item.title}?`)) {
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<void>(`/subscriptions/${item.id}`, { method: "DELETE" });
      await loadSubscriptions(page, size, query);
      setMessage("Xóa subscription thành công.");
      pushToast("Xóa subscription thành công.", "success");
      if (selected?.id === item.id) {
        setSelected(null);
        setForm({ title: "", description: "", status: 1 });
      }
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Xóa subscription thất bại.");
        pushToast("Xóa subscription thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Quan ly subscription</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">CRUD tenant subscription voi ownership rule theo RBAC/ABAC hien tai.</p>
      </div>

      <Card>
        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitSearch}>
          <Field label="Tìm kiếm subscription">
            <TextInput
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Nhập tiêu đề hoặc mô tả"
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

      <div className="grid gap-5 xl:grid-cols-[1.2fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sách subscription</h2>
          <Table
            headers={["ID", "Title", "Owner", "Status", "Action"]}
            rows={(subscriptionPage?.content ?? []).map((item) => [
              item.id,
              <div key={`title-${item.id}`}>
                <p className="font-semibold">{item.title}</p>
                <p className="text-xs text-[var(--muted)]">{item.description}</p>
              </div>,
              <span key={`owner-${item.id}`} className="font-mono text-xs text-[var(--muted)]">
                {item.ownerUserId}
              </span>,
              <Badge key={`status-${item.id}`} variant={item.status === 1 ? "success" : "danger"}>
                {toVietnameseStatus(item.status)}
              </Badge>,
              <div key={`actions-${item.id}`} className="flex flex-wrap gap-2">
                {canUpdate ? (
                  <Button variant="secondary" onClick={() => onPick(item)}>
                    Sua
                  </Button>
                ) : null}
                {canDelete ? (
                  <Button variant="danger" onClick={() => onDelete(item)}>
                    Xóa
                  </Button>
                ) : null}
              </div>,
            ])}
          />

          <Pagination
            page={subscriptionPage?.number ?? 0}
            totalPages={Math.max(subscriptionPage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
            size={size}
            onSizeChange={(nextSize) => {
              setSize(nextSize);
              setPage(0);
            }}
          />

          {loading ? <p className="text-sm text-[var(--muted)]">Đang tải dữ liệu...</p> : null}

          {!isSuperAdmin ? (
            <Alert
              variant="info"
              message="Bạn đang ở chế độ ownership. Chỉ dữ liệu do bạn tạo mới hiển thị và chỉnh sửa được."
            />
          ) : null}
        </Card>

        <div className="space-y-5">
          <Card className="space-y-4">
            <h2 className="text-lg font-semibold">{selected ? "Cập nhật" : "Tạo mới"} subscription</h2>
            <form className="space-y-3" onSubmit={onSubmit}>
              <Field label="Title">
                <TextInput value={form.title} onChange={(event) => setForm((prev) => ({ ...prev, title: event.target.value }))} required />
              </Field>
              <Field label="Description">
                <TextAreaInput
                  rows={3}
                  value={form.description}
                  onChange={(event) => setForm((prev) => ({ ...prev, description: event.target.value }))}
                />
              </Field>
              <Field label="Status">
                <SelectInput value={form.status} onChange={(event) => setForm((prev) => ({ ...prev, status: Number(event.target.value) }))}>
                  <option value={1}>Hoat dong</option>
                  <option value={2}>Ngung hoat dong</option>
                </SelectInput>
              </Field>
              <div className="flex flex-wrap gap-2">
                <Button type="submit" loading={saving} disabled={selected ? !canUpdate : !canAdd}>
                  {selected ? "Lưu cập nhật" : "Tạo mới"}
                </Button>
                {selected ? (
                  <Button
                    variant="ghost"
                    onClick={() => {
                      setSelected(null);
                      setForm({ title: "", description: "", status: 1 });
                    }}
                  >
                    Huy chon
                  </Button>
                ) : null}
              </div>
            </form>
          </Card>

          <Card>
            <p className="text-sm text-[var(--muted)] leading-7">
              Module subscription da duoc dọn bo luong import/export demo. Uu tien nghiep vu tenant-boundary va ownership.
            </p>
          </Card>
        </div>
      </div>
    </div>
  );
}
