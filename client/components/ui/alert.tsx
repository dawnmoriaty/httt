import { classNames } from "@/lib/format";

type AlertVariant = "success" | "error" | "info";

type AlertProps = {
  message: string;
  variant?: AlertVariant;
};

const variants: Record<AlertVariant, string> = {
  success: "border-emerald-200 bg-emerald-50 text-emerald-700",
  error: "border-rose-200 bg-rose-50 text-rose-700",
  info: "border-sky-200 bg-sky-50 text-sky-700",
};

export function Alert({ message, variant = "info" }: AlertProps) {
  return <div className={classNames("rounded-xl border px-4 py-3 text-sm", variants[variant])}>{message}</div>;
}
