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
  const [size, setSize] = useState(10);
  const [memberSize, setMemberSize] = useState(10);
  const [query, setQuery] = useState("");
  const [memberQuery, setMemberQuery] = useState("");
  const [searchInput, setSearchInput] = useState("");
  const [memberSearchInput, setMemberSearchInput] = useState("");
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

  const loadTenantGroups = async (targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<TenantGroup>>(`/tenant-groups?${queryString}`);
    setTenantGroupPage(data);
  };

  const loadUsers = async () => {
    const data = await apiRequest<PageData<User>>(`/admin/users?page=0&size=200`);
    setUsers(data.content);
  };

  const loadMembers = async (tenantGroupId: number, targetPage: number, targetSize: number, targetQuery: string) => {
    const queryString = buildPagingQuery({ page: targetPage, size: targetSize, q: targetQuery });
    const data = await apiRequest<PageData<TenantGroupMember>>(`/tenant-groups/${tenantGroupId}/members?${queryString}`);
    setMemberResult(data);
  };

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        await Promise.all([loadTenantGroups(page, size, query), loadUsers()]);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải dữ liệu nhóm người thuê.");
          pushToast("Không thể tải dữ liệu nhóm người thuê.", "error");
        }
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [page, size, query, pushToast]);

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

  const onSubmitMemberSearch = (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMemberPage(0);
    setMemberQuery(memberSearchInput);
  };

  const onClearMemberSearch = () => {
    setMemberSearchInput("");
    setMemberQuery("");
    setMemberPage(0);
  };

  useEffect(() => {
    if (!selected) {
      setMemberResult(null);
      return;
    }

    const run = async () => {
      try {
        await loadMembers(selected.id, memberPage, memberSize, memberQuery);
      } catch (apiError) {
        if (apiError instanceof ApiClientError) {
          setError(apiError.message);
          pushToast(apiError.message, "error");
        } else {
          setError("Không thể tải danh sách thành viên.");
          pushToast("Không thể tải danh sách thành viên.", "error");
        }
      }
    };

    void run();
  }, [selected, memberPage, memberSize, memberQuery, pushToast]);

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
    setMemberQuery("");
    setMemberSearchInput("");
    setForm({
      code: item.code,
      name: item.name,
      representativeUserId: item.representativeUserId,
      status: item.status,
      note: item.note ?? "",
    });

    try {
      await loadMembers(item.id, 0, memberSize, "");
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
      setError("Vui lòng chọn người đại diện.");
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
        setMessage("Cập nhật nhóm người thuê thành công.");
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
        setMessage("Tạo nhóm người thuê thành công.");
        pushToast("Tạo nhóm người thuê thành công.", "success");
        resetTenantForm();
      }

      await loadTenantGroups(page, size, query);
      if (selected) {
        const refreshed = await apiRequest<TenantGroup>(`/tenant-groups/${selected.id}`);
        setSelected(refreshed);
      }
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Lưu nhóm người thuê thất bại.");
        pushToast("Lưu nhóm người thuê thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onDeleteTenantGroup = async (item: TenantGroup) => {
    if (!window.confirm(`Xóa nhóm ${item.name}?`)) {
      return;
    }

    setSaving(true);
    setError(null);
    setMessage(null);

    try {
      await apiRequest<void>(`/tenant-groups/${item.id}`, { method: "DELETE" });
      await loadTenantGroups(page, size, query);
      if (selected?.id === item.id) {
        resetTenantForm();
        setMemberResult(null);
      }
      setMessage("Xóa nhóm người thuê thành công.");
      pushToast("Xóa nhóm người thuê thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Xóa nhóm người thuê thất bại.");
        pushToast("Xóa nhóm người thuê thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  const onAddMember = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!selected) {
      setError("Hãy chọn nhóm trước khi thêm thành viên.");
      return;
    }
    if (memberForm.userId === "") {
      setError("Vui lòng chọn người dùng thành viên.");
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
      await loadMembers(selected.id, memberPage, memberSize, memberQuery);
      await loadTenantGroups(page, size, query);
      setMessage("Thêm thành viên thành công.");
      pushToast("Thêm thành viên thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Thêm thành viên thất bại.");
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

    if (!window.confirm(`Xóa thành viên ${member.fullName}?`)) {
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await apiRequest<void>(`/tenant-groups/${selected.id}/members/${member.id}`, {
        method: "DELETE",
      });
      await loadMembers(selected.id, memberPage, memberSize, memberQuery);
      await loadTenantGroups(page, size, query);
      setMessage("Xóa thành viên thành công.");
      pushToast("Xóa thành viên thành công.", "success");
    } catch (apiError) {
      if (apiError instanceof ApiClientError) {
        setError(apiError.message);
        pushToast(apiError.message, "error");
      } else {
        setError("Xóa thành viên thất bại.");
        pushToast("Xóa thành viên thất bại.", "error");
      }
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="space-y-5">
      <div>
        <h1 className="text-2xl font-bold text-[var(--foreground)]">Quản lý người thuê</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">
          Quản lý nhóm người thuê theo hộ gia đình, cập nhật người đại diện và danh sách thành viên.
        </p>
      </div>

      <Card>
        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitSearch}>
          <Field label="Tìm kiếm nhóm người thuê">
            <TextInput
              value={searchInput}
              onChange={(event) => setSearchInput(event.target.value)}
              placeholder="Nhập mã nhóm, tên nhóm hoặc ghi chú"
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
          <h2 className="text-lg font-semibold">Danh sách nhóm người thuê</h2>
          <Table
            headers={["ID", "Mã", "Tên nhóm", "Đại diện", "Số TV", "Trạng thái", "Thao tác"]}
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
                  Chọn
                </Button>
                <Button variant="danger" onClick={() => void onDeleteTenantGroup(item)} disabled={saving}>
                  Xóa
                </Button>
              </div>,
            ])}
          />

          <Pagination
            page={tenantGroupPage?.number ?? 0}
            totalPages={Math.max(tenantGroupPage?.totalPages ?? 0, 1)}
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
          <h2 className="text-lg font-semibold">{selected ? "Cập nhật nhóm" : "Tạo nhóm mới"}</h2>
          <form className="space-y-3" onSubmit={onSaveTenantGroup}>
            <Field label="Mã nhóm">
              <TextInput
                value={form.code}
                onChange={(event) => setForm((prev) => ({ ...prev, code: event.target.value }))}
                required
                disabled={!!selected}
              />
            </Field>
            <Field label="Tên nhóm">
              <TextInput
                value={form.name}
                onChange={(event) => setForm((prev) => ({ ...prev, name: event.target.value }))}
                required
              />
            </Field>
            <Field label="Người đại diện">
              <SelectInput
                value={form.representativeUserId}
                onChange={(event) => setForm((prev) => ({ ...prev, representativeUserId: Number(event.target.value) }))}
                required
              >
                <option value="" disabled>
                  Chọn người dùng
                </option>
                {users.map((user) => (
                  <option key={user.id} value={user.id}>
                    {user.fullName} ({user.username})
                  </option>
                ))}
              </SelectInput>
            </Field>
            <Field label="Trạng thái">
              <SelectInput
                value={form.status}
                onChange={(event) => setForm((prev) => ({ ...prev, status: Number(event.target.value) }))}
              >
                <option value={1}>Hoạt động</option>
                <option value={2}>Ngừng hoạt động</option>
              </SelectInput>
            </Field>
            <Field label="Ghi chú">
              <TextInput value={form.note} onChange={(event) => setForm((prev) => ({ ...prev, note: event.target.value }))} />
            </Field>
            <div className="flex flex-wrap gap-2">
              <Button type="submit" loading={saving}>
                {selected ? "Lưu cập nhật" : "Tạo nhóm"}
              </Button>
              {selected ? (
                <Button variant="ghost" onClick={resetTenantForm}>
                  Hủy chọn
                </Button>
              ) : null}
            </div>
          </form>
        </Card>
      </div>

      <Card className="space-y-4">
        <div className="flex items-center justify-between gap-2">
          <h2 className="text-lg font-semibold">Thành viên nhóm</h2>
          <span className="text-sm text-[var(--muted)]">
            Nhóm đang chọn: <span className="font-semibold text-[var(--foreground)]">{selected?.name ?? "Chưa chọn"}</span>
          </span>
        </div>

        <form className="flex flex-wrap items-end gap-3" onSubmit={onSubmitMemberSearch}>
          <Field label="Tìm kiếm thành viên trong nhóm">
            <TextInput
              value={memberSearchInput}
              onChange={(event) => setMemberSearchInput(event.target.value)}
              placeholder="Nhập tên, username, email hoặc CCCD"
              className="min-w-72"
              disabled={!selected}
            />
          </Field>
          <div className="flex gap-2">
            <Button type="submit" variant="secondary" disabled={!selected}>
              Tìm
            </Button>
            <Button type="button" variant="ghost" onClick={onClearMemberSearch} disabled={!selected}>
              Xóa lọc
            </Button>
          </div>
        </form>

        <form className="grid gap-3 md:grid-cols-5" onSubmit={onAddMember}>
          <Field label="Người dùng">
            <SelectInput
              value={memberForm.userId}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, userId: Number(event.target.value) }))}
              disabled={!selected}
              required
            >
              <option value="" disabled>
                Chọn người dùng
              </option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.fullName}
                </option>
              ))}
              </SelectInput>
            </Field>
          <Field label="Vai trò">
            <SelectInput
              value={memberForm.memberRole}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, memberRole: Number(event.target.value) }))}
            >
              <option value={1}>Đại diện</option>
              <option value={2}>Thành viên</option>
            </SelectInput>
          </Field>
          <Field label="Ngày vào ở">
            <TextInput
              type="date"
              value={memberForm.joinedAt}
              onChange={(event) => setMemberForm((prev) => ({ ...prev, joinedAt: event.target.value }))}
            />
          </Field>
          <Field label="Ngày rời đi">
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
              Thêm thành viên
            </Button>
          </div>
        </form>

        <Table
          headers={["ID", "Người dùng", "Email", "Vai trò", "Ngày vào", "Ngày ra", "CCCD", "Thao tác"]}
          rows={(memberResult?.content ?? []).map((member) => [
            member.id,
            member.fullName,
            member.email,
            member.memberRole === 1 ? "Đại diện" : "Thành viên",
            member.joinedAt ?? "-",
            member.leftAt ?? "-",
            member.idCardNumber ?? "-",
            <Button key={`del-member-${member.id}`} variant="danger" onClick={() => void onRemoveMember(member)} disabled={!selected || saving}>
              Xóa
            </Button>,
          ])}
        />

        <Pagination
          page={memberResult?.number ?? 0}
          totalPages={Math.max(memberResult?.totalPages ?? 0, 1)}
          onPageChange={(nextPage) => setMemberPage(nextPage)}
          size={memberSize}
          onSizeChange={(nextSize) => {
            setMemberSize(nextSize);
            setMemberPage(0);
          }}
        />
      </Card>
    </div>
  );
}
