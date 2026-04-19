"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { AuthApp } from "@/components/auth/auth-app";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, TextInput } from "@/components/ui/field";
import { ApiClientError } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";
import { useToast } from "@/components/ui/toast-provider";

function LoginForm() {
  const router = useRouter();
  const { login, isAuthenticated, loading } = useAuth();

  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("admin123");
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);
  const { pushToast } = useToast();

  if (!loading && isAuthenticated) {
    router.replace("/dashboard");
    return null;
  }

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessage(null);
    setSubmitting(true);

    try {
      await login(username, password);
      pushToast("Đăng nhập thành công.", "success");
      router.replace("/dashboard");
    } catch (error) {
      if (error instanceof ApiClientError) {
        setMessage(error.message);
        pushToast(error.message, "error");
      } else {
        setMessage("Đăng nhập thất bại. Vui lòng thử lại.");
        pushToast("Đăng nhập thất bại. Vui lòng thử lại.", "error");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,_#d6eefe_0%,_#eef5fb_45%,_#f3f7fb_100%)] px-4 py-8">
      <Card className="w-full max-w-md space-y-6 p-7">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-[var(--muted)]">HTTT Monolith</p>
          <h1 className="text-2xl font-bold text-[var(--foreground)]">Đăng nhập hệ thống RBAC</h1>
          <p className="text-sm text-[var(--muted)]">Sử dụng tài khoản admin seeded để quản trị role, user, permission và module người thuê.</p>
        </div>

        {message ? <Alert variant="error" message={message} /> : null}

        <form className="space-y-4" onSubmit={onSubmit}>
          <Field label="Username">
            <TextInput value={username} onChange={(event) => setUsername(event.target.value)} placeholder="admin" required />
          </Field>

          <Field label="Password">
            <TextInput
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="admin123"
              required
            />
          </Field>

          <Button type="submit" className="w-full" loading={submitting}>
            Đăng nhập
          </Button>

          <div className="pt-1 text-right">
            <Link href="/register" className="text-sm font-semibold text-[var(--primary)] hover:underline">
              Chưa có tài khoản? Đăng ký
            </Link>
          </div>
        </form>
      </Card>
    </main>
  );
}

export default function LoginPage() {
  return (
    <AuthApp>
      <LoginForm />
    </AuthApp>
  );
}
