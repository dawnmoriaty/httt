"use client";

import type { ButtonHTMLAttributes, ReactNode } from "react";
import { classNames } from "@/lib/format";

type ButtonVariant = "primary" | "secondary" | "ghost" | "danger";

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: ButtonVariant;
  loading?: boolean;
  icon?: ReactNode;
};

const variantStyles: Record<ButtonVariant, string> = {
  primary:
    "border border-transparent bg-[var(--primary)] text-white shadow-[0_8px_24px_rgba(14,122,191,0.25)] hover:bg-[var(--primary-strong)]",
  secondary: "border border-[var(--border)] bg-white text-[var(--foreground)] hover:bg-[var(--surface-muted)]",
  ghost: "border border-transparent bg-transparent text-[var(--muted)] hover:bg-[var(--surface-muted)]",
  danger: "border border-transparent bg-[var(--danger)] text-white hover:opacity-90",
};

export function Button({
  type = "button",
  className,
  variant = "primary",
  loading = false,
  disabled,
  children,
  icon,
  ...props
}: ButtonProps) {
  return (
    <button
      type={type}
      disabled={disabled || loading}
      className={classNames(
        "inline-flex items-center justify-center gap-2 rounded-xl px-4 py-2.5 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-50",
        variantStyles[variant],
        className,
      )}
      {...props}
    >
      {loading ? <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/70 border-t-transparent" /> : icon}
      <span>{children}</span>
    </button>
  );
}
