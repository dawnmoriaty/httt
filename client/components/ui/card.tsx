import type { ReactNode } from "react";
import { classNames } from "@/lib/format";

type CardProps = {
  children: ReactNode;
  className?: string;
};

export function Card({ children, className }: CardProps) {
  return (
    <section
      className={classNames(
        "rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-5 shadow-[0_14px_34px_rgba(16,33,51,0.08)]",
        className,
      )}
    >
      {children}
    </section>
  );
}
