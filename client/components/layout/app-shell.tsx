"use client";

import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import { classNames } from "@/lib/format";
import { useAuth } from "@/lib/auth-context";

type NavItem = {
  href: string;
  label: string;
  resource: string;
  action: string;
};

const navItems: NavItem[] = [
  { href: "/dashboard", label: "Tong quan", resource: "subscription", action: "VIEW" },
  { href: "/admin/roles", label: "Nhom quyen", resource: "role", action: "VIEW" },
  { href: "/admin/permissions", label: "Danh muc quyen", resource: "permission", action: "VIEW" },
  { href: "/admin/users", label: "Tai khoan", resource: "user", action: "VIEW" },
  { href: "/subscriptions", label: "Subscription", resource: "subscription", action: "VIEW" },
];

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const router = useRouter();
  const { user, hasPermission, logout } = useAuth();
  const [menuOpen, setMenuOpen] = useState(false);

  const menus = useMemo(
    () => navItems.filter((item) => hasPermission(item.resource, item.action)),
    [hasPermission],
  );

  const handleLogout = async () => {
    await logout();
    router.replace("/login");
  };

  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_top_left,_#dff0ff_0%,_#f4f8fc_30%,_#f3f7fb_100%)] text-[var(--foreground)]">
      <header className="sticky top-0 z-30 border-b border-[var(--border)] bg-white/85 backdrop-blur">
        <div className="mx-auto flex h-16 w-full max-w-[1400px] items-center justify-between px-4 sm:px-6">
          <div className="flex items-center gap-3">
            <button
              type="button"
              className="rounded-lg border border-[var(--border)] px-3 py-1.5 text-sm font-semibold text-[var(--muted)] lg:hidden"
              onClick={() => setMenuOpen((value) => !value)}
            >
              Menu
            </button>
            <div>
              <p className="text-sm font-semibold tracking-wide text-[var(--muted)]">HTTT RBAC</p>
              <p className="text-base font-bold">Admin Console</p>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="text-sm font-semibold">{user?.fullName ?? "Unknown"}</p>
              <p className="text-xs text-[var(--muted)]">{user?.selectedRoleCode ?? "NO_ROLE"}</p>
            </div>
            <Button variant="secondary" onClick={handleLogout}>
              Dang xuat
            </Button>
          </div>
        </div>
      </header>

      <div className="mx-auto flex w-full max-w-[1400px] gap-5 px-4 py-6 sm:px-6">
        <aside
          className={classNames(
            "fixed inset-y-16 left-0 z-20 w-72 border-r border-[var(--border)] bg-white px-4 py-4 shadow-[0_10px_30px_rgba(16,33,51,0.08)] transition lg:static lg:inset-auto lg:block lg:w-72 lg:rounded-2xl lg:border lg:shadow-none",
            menuOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0",
          )}
        >
          <nav className="flex flex-col gap-1">
            {menus.map((item) => {
              const active = pathname === item.href || pathname.startsWith(`${item.href}/`);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  onClick={() => setMenuOpen(false)}
                  className={classNames(
                    "rounded-xl px-3 py-2.5 text-sm font-semibold transition",
                    active
                      ? "bg-[color:rgba(14,122,191,0.12)] text-[var(--primary)]"
                      : "text-[var(--muted)] hover:bg-[var(--surface-muted)] hover:text-[var(--foreground)]",
                  )}
                >
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </aside>

        <main className="min-w-0 flex-1">{children}</main>
      </div>
    </div>
  );
}
