import type { ReactNode } from "react";
import { classNames } from "@/lib/format";

type BadgeVariant = "neutral" | "success" | "danger" | "info";

type BadgeProps = {
  children: ReactNode;
  variant?: BadgeVariant;
};

const variantStyles: Record<BadgeVariant, string> = {
  neutral: "bg-slate-100 text-slate-700",
  success: "bg-emerald-100 text-emerald-700",
  danger: "bg-rose-100 text-rose-700",
  info: "bg-sky-100 text-sky-700",
};

export function Badge({ children, variant = "neutral" }: BadgeProps) {
  return <span className={classNames("inline-flex rounded-full px-2.5 py-1 text-xs font-semibold", variantStyles[variant])}>{children}</span>;
}
