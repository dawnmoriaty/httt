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
import type { PageData, TenantGroup, TenantGroupMember, User } from "@/lib/types";

type TenantGroupForm = {
  code: string;
  name: string;
  representativeUserId: number | "";
  status: number;
  note: string;
};

type TenantMemberForm = {
  userId: number | "";
  memberRole: number;
  joinedAt: string;
  leftAt: string;
  idCardNumber: string;
};

export default function TenantGroupsPage() {
  return (
    <AuthGuard requiredPermission={{ resource: "tenant_group", action: "VIEW" }}>
      <TenantGroupsPageContent />
    </AuthGuard>
  );
}

function TenantGroupsPageContent() {
  const [page, setPage] = useState(0);
  const [memberPage, setMemberPage] = useState(0);
  const [tenantGroupPage, setTenantGroupPage] = useState<PageData<TenantGroup> | null>(null);
  const [memberResult, setMemberResult] = useState<PageData<TenantGroupMember> | null>(null);
  const [users, setUsers] = useState<User[]>([]);
  const [selected, setSelected] = useState<TenantGroup | null>(null);
  const [form, setForm] = useState<TenantGroupForm>({
    code: "",
    name: "",
    representativeUserId: "",
    status: 1,
    note: "",
  });
  const [memberForm, setMemberForm] = useState<TenantMemberForm>({
    userId: "",
    memberRole: 2,
    joinedAt: "",
    leftAt: "",
    idCardNumber: "",
  });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();

  const loadTenantGroups = async (targetPage: number) => {
    const data = await apiRequest<PageData<TenantGroup>>(`/tenant-groups?page=${targetPage}&size=10`);
    setTenantGroupPage(data);
  };

  const loadUsers = async () => {
    const data = await apiRequest<PageData<User>>(`/admin/users?page=0&size=200`);
    setUsers(data.content);
  };

  const loadMembers = async (tenantGroupId: number, targetPage: number) => {
    const data = await apiRequest<PageData<TenantGroupMember>>(
      `/tenant-groups/${tenantGroupId}/members?page=${targetPage}&size=10`,
    );
    setMemberResult(data);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await Promise.all([loadTenantGroups(page), loadUsers()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Khong the tai du lieu nhom nguoi thue.");
          pushToast("Không thể tải dữ liệu nhóm người thuê.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page, pushToast]);

  useEffect(() => {
    if (!selected) {
      setMemberResult(null);
      return;
    }

    const run = async () => {
      try {
        await loadMembers(selected.id, memberPage);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Khong the tai danh sach thanh vien.");
          pushToast("Không thể tải danh sách thành viên.", "error");
        }
      }
    };

    void run();
  }, [selected, memberPage, pushToast]);

  const resetTenantForm = () => {
    setForm({
      code: "",
      name: "",
      representativeUserId: "",
      status: 1,
      note: "",
    });
    setSelected(null);
  };

  const onPick = async (item: TenantGroup) => {
    setSelected(item);
    setMemberPage(0);
    setForm({
      code: item.code,
      name: item.name,
      representativeUserId: item.representativeUserId,
      status: item.status,
      note: item.note ?? "",
    });

    try {
      await loadMembers(item.id, 0);
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      }
    }
  };

  const onSaveTenantGroup = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (form.representativeUserId === "") {
      setError("Vui long chon nguoi dai dien.");
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      if (selected) {
        await apiRequest<TenantGroup>(`/tenant-groups/${selected.id}`, {
          method: "PUT",
          body: {
            name: form.name,
            representativeUserId: form.representativeUserId,
            status: form.status,
            note: form.note,
          },
        });
        setMessage("Cap nhat nhom nguoi thue thanh cong.");
        pushToast("Cập nhật nhóm người thuê thành công.", "success");
      } else {
        await apiRequest<TenantGroup>("/tenant-groups", {
          method: "POST",
          body: {
            code: form.code,
            name: form.name,
            representativeUserId: form.representativeUserId,
            status: form.status,
            note: form.note,
          },
        });
        setMessage("Tao nhom nguoi thue thanh cong.");
        pushToast("Tạo nhóm người thuê thành công.", "success");
        resetTenantForm();
      }

      await loadTenantGroups(page);
      if (selected) {
        const refreshed = await apiRequest<TenantGroup>(`/tenant-groups/${selected.id}`);
        setSelected(refreshed);
      }
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Luu nhom nguoi thue that bai.");
        pushToast("Lưu nhóm người thuê thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onDeleteTenantGroup = async (item: TenantGroup) => {
    if (!window.confirm(`Xoa nhom ${item.name}?`)) {
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<void>(`/tenant-groups/${item.id}`, { method: "DELETE" });
      await loadTenantGroups(page);
      if (selected?.id === item.id) {
        resetTenantForm();
        setMemberResult(null);
      }
      setMessage("Xoa nhom nguoi thue thanh cong.");
      pushToast("Xóa nhóm người thuê thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Xoa nhom nguoi thue that bai.");
        pushToast("Xóa nhóm người thuê thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onAddMember = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selected) {
      setError("Hay chon nhom truoc khi them thanh vien.");
      return;
    }
    if (memberForm.userId === "") {
      setError("Vui long chon user thanh vien.");
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<TenantGroupMember>(`/tenant-groups/${selected.id}/members`, {
        method: "POST",
        body: {
          userId: memberForm.userId,
          memberRole: memberForm.memberRole,
          joinedAt: memberForm.joinedAt,
          leftAt: memberForm.leftAt,
          idCardNumber: memberForm.idCardNumber,
        },
      });

      setMemberForm({
        userId: "",
        memberRole: 2,
        joinedAt: "",
        leftAt: "",
        idCardNumber: "",
      });
      await loadMembers(selected.id, memberPage);
      await loadTenantGroups(page);
      setMessage("Them thanh vien thanh cong.");
      pushToast("Thêm thành viên thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Them thanh vien that bai.");
        pushToast("Thêm thành viên thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onRemoveMember = async (member: TenantGroupMember) => {
    if (!selected) {
      return;
    }

    if (!window.confirm(`Xoa thanh vien ${member.fullName}?`)) {
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await apiRequest<void>(`/tenant-groups/${selected.id}/members/${member.id}`, {
        method: "DELETE",
      });
      await loadMembers(selected.id, memberPage);
      await loadTenantGroups(page);
      setMessage("Xoa thanh vien thanh cong.");
      pushToast("Xóa thành viên thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Xoa thanh vien that bai.");
        pushToast("Xóa thành viên thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Quan ly nguoi thue</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">
          Quan ly nhom nguoi thue theo ho gia dinh, cap nhat nguoi dai dien va danh sach thanh vien.
        </p>
      </div>

      {error ? <Alert variant="error" message={error} /> : null}
      {message ? <Alert variant="success" message={message} /> : null}

      <div className="grid gap-5 xl:grid-cols-[1.2fr_1fr]">
        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">Danh sach nhom nguoi thue</h2>
          <Table
            headers={["ID", "Ma", "Ten nhom", "Dai dien", "So TV", "Status", "Action"]}
            rows={(tenantGroupPage?.content ?? []).map((item) => [
              item.id,
              <span key={`code-${item.id}`} className="font-mono text-xs">
                {item.code}
              </span>,
              item.name,
              item.representativeFullName ?? item.representativeUserId,
              item.memberCount,
              <Badge key={`status-${item.id}`} variant={item.status === 1 ? "success" : "danger"}>
                {toVietnameseStatus(item.status)}
              </Badge>,
              <div key={`actions-${item.id}`} className="flex flex-wrap gap-2">
                <Button variant="secondary" onClick={() => void onPick(item)}>
                  Chon
                </Button>
                <Button variant="danger" onClick={() => void onDeleteTenantGroup(item)} disabled={saving}>
                  Xoa
                </Button>
              </div>,
            ])}
          />

          <Pagination
            page={tenantGroupPage?.number ?? 0}
            totalPages={Math.max(tenantGroupPage?.totalPages ?? 0, 1)}
            onPageChange={(nextPage) => setPage(nextPage)}
          />

          {loading ? <p className="text-sm text-[var(--muted)]">Dang tai du lieu...</p> : null}
        </Card>

        <Card className="space-y-4">
          <h2 className="text-lg font-semibold">{selected ? "Cap nhat nhom" : "Tao nhom moi"}</h2>
          <form className="space-y-3" onSubmit={onSaveTenantGroup}>
            <Field label="Ma nhom">
              <TextInput
                value={form.code}
                onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))}
                required
                disabled={!!selected}
              />
            </Field>
            <Field label="Ten nhom">
              <TextInput
                value={form.name}
                onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
                required
              />
            </Field>
            <Field label="Nguoi dai dien">
              <SelectInput
                value={form.representativeUserId}
                onChange={(event) => setForm((prev) => ({ ...prev, representativeUserId: Number(event.target.value) }))}
                required
              >
                <option value="" disabled>
                  Chon user
                </option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.fullName} ({user.username})
                  </option>
                ))}
              </SelectInput>
            </Field>
            <Field label="Trang thai">
              <SelectInput
                value={form.status}
                onChange={(event) => setForm((prev) => ({ ...prev, status: Number(event.target.value) }))}
              >
                <option value={1}>Hoat dong</option>
                <option value={2}>Ngung hoat dong</option>
              </SelectInput>
            </Field>
            <Field label="Ghi chu">
              <TextInput value={form.note} onChange={(event) => setForm((prev) => ({ ...prev, note: event.target.value }))} />
            </Field>
            <div className="flex flex-wrap gap-2">
              <Button type="submit" loading={saving}>
                {selected ? "Luu cap nhat" : "Tao nhom"}
              </Button>
              {selected ? (
                <Button variant="ghost" onClick={resetTenantForm}>
                  Huy chon
                </Button>
              ) : null}
            </div>
          </form>
        </Card>
      </div>

      <Card className="space-y-4">
        <div className="flex items-center justify-between gap-2">
          <h2 className="text-lg font-semibold">Thanh vien nhom</h2>
          <span className="text-sm text-[var(--muted)]">
            Nhom dang chon: <span className="font-semibold text-[var(--foreground)]">{selected?.name ?? "Chua chon"}</span>
          </span>
        </div>

        <form className="grid gap-3 md:grid-cols-5" onSubmit={onAddMember}>
          <Field label="User">
            <SelectInput
              value={memberForm.userId}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, userId: Number(event.target.value) }))}
              disabled={!selected}
              required
            >
              <option value="" disabled>
                Chon user
              </option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.fullName}
                </option>
              ))}
            </SelectInput>
          </Field>
          <Field label="Vai tro">
            <SelectInput
              value={memberForm.memberRole}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, memberRole: Number(event.target.value) }))}
            >
              <option value={1}>Dai dien</option>
              <option value={2}>Thanh vien</option>
            </SelectInput>
          </Field>
          <Field label="Ngay vao o">
            <TextInput
              type="date"
              value={memberForm.joinedAt}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, joinedAt: event.target.value }))}
            />
          </Field>
          <Field label="Ngay roi di">
            <TextInput
              type="date"
              value={memberForm.leftAt}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, leftAt: event.target.value }))}
            />
          </Field>
          <Field label="CCCD">
            <TextInput
              value={memberForm.idCardNumber}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, idCardNumber: event.target.value }))}
            />
          </Field>
          <div className="md:col-span-5">
            <Button type="submit" disabled={!selected} loading={saving}>
              Them thanh vien
            </Button>
          </div>
        </form>

        <Table
          headers={["ID", "User", "Email", "Vai tro", "Ngay vao", "Ngay ra", "CCCD", "Action"]}
          rows={(memberResult?.content ?? []).map((member) => [
            member.id,
            member.fullName,
            member.email,
            member.memberRole === 1 ? "Dai dien" : "Thanh vien",
            member.joinedAt ?? "-",
            member.leftAt ?? "-",
            member.idCardNumber ?? "-",
            <Button key={`del-member-${member.id}`} variant="danger" onClick={() => void onRemoveMember(member)} disabled={!selected || saving}>
              Xoa
            </Button>,
          ])}
        />

        <Pagination
          page={memberResult?.number ?? 0}
          totalPages={Math.max(memberResult?.totalPages ?? 0, 1)}
          onPageChange={(nextPage) => setMemberPage(nextPage)}
        />
      </Card>
    </div>
  );
}
