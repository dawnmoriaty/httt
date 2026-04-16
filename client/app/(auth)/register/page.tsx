"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { AuthApp } from "@/components/auth/auth-app";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Field, TextInput } from "@/components/ui/field";
import { useToast } from "@/components/ui/toast-provider";
import { ApiClientError } from "@/lib/api-client";
import { useAuth } from "@/lib/auth-context";

function RegisterForm() {
  const router = useRouter();
  const { register, isAuthenticated, loading } = useAuth();
  const { pushToast } = useToast();

  const [username, setUsername] = useState("");
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState<string | null>(null);

  if (!loading && isAuthenticated) {
    router.replace("/dashboard");
    return null;
  }

  const onSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setMessage(null);
    setSubmitting(true);

    try {
      await register({ username, fullName, email, password });
      pushToast("Đăng ký thành công.", "success");
      router.replace("/dashboard");
    } catch (error) {
      if (error instanceof ApiClientError) {
        setMessage(error.message);
        pushToast(error.message, "error");
      } else {
        setMessage("Đăng ký thất bại. Vui lòng thử lại.");
        pushToast("Đăng ký thất bại. Vui lòng thử lại.", "error");
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,_#d6eefe_0%,_#eef5fb_45%,_#f3f7fb_100%)] px-4 py-8">
      <Card className="w-full max-w-xl space-y-6 p-7">
        <div className="space-y-2">
          <p className="text-xs font-semibold uppercase tracking-[0.22em] text-[var(--muted)]">HTTT Monolith</p>
          <h1 className="text-2xl font-bold text-[var(--foreground)]">Tạo tài khoản mới</h1>
          <p className="text-sm text-[var(--muted)]">Đăng ký user mới để hệ thống tự gán role mặc định USER.</p>
        </div>

        {message ? <Alert variant="error" message={message} /> : null}

        <form className="grid gap-4 sm:grid-cols-2" onSubmit={onSubmit}>
          <div className="sm:col-span-1">
            <Field label="Username">
              <TextInput value={username} onChange={(event) => setUsername(event.target.value)} required />
            </Field>
          </div>
          <div className="sm:col-span-1">
            <Field label="Họ tên">
              <TextInput value={fullName} onChange={(event) => setFullName(event.target.value)} required />
            </Field>
          </div>

          <div className="sm:col-span-2">
            <Field label="Email">
              <TextInput type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
            </Field>
          </div>

          <div className="sm:col-span-2">
            <Field label="Mật khẩu">
              <TextInput type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
            </Field>
          </div>

          <div className="sm:col-span-2 flex items-center justify-between gap-3">
            <Link href="/login" className="text-sm font-semibold text-[var(--primary)] hover:underline">
              Quay lại đăng nhập
            </Link>
            <Button type="submit" loading={submitting}>
              Đăng ký
            </Button>
          </div>
        </form>
      </Card>
    </main>
  );
}

export default function RegisterPage() {
  return (
    <AuthApp>
      <RegisterForm />
    </AuthApp>
  );
}
