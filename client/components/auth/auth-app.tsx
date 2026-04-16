"use client";

import { AuthProvider } from "@/lib/auth-context";
import { ToastProvider } from "@/components/ui/toast-provider";

export function AuthApp({ children }: { children: React.ReactNode }) {
  return (
    <ToastProvider>
      <AuthProvider>{children}</AuthProvider>
    </ToastProvider>
  );
}
