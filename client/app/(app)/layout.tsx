import { AuthApp } from "@/components/auth/auth-app";
import { AuthGuard } from "@/components/auth/auth-guard";
import { AppShell } from "@/components/layout/app-shell";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <AuthApp>
      <AuthGuard>
        <AppShell>{children}</AppShell>
      </AuthGuard>
    </AuthApp>
  );
}
