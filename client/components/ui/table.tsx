import type { ReactNode } from "react";
import { classNames } from "@/lib/format";

type TableProps = {
  headers: ReactNode[];
  rows: ReactNode[][];
  className?: string;
};

export function Table({ headers, rows, className }: TableProps) {
  return (
    <div className={classNames("overflow-x-auto rounded-xl border border-[var(--border)]", className)}>
      <table className="min-w-full divide-y divide-[var(--border)] bg-white text-sm">
        <thead className="bg-[var(--surface-muted)]">
          <tr>
            {headers.map((header, index) => (
              <th key={index} className="whitespace-nowrap px-4 py-3 text-left font-semibold text-[var(--foreground)]">
                {header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-[var(--border)]">
          {rows.length > 0 ? (
            rows.map((row, rowIndex) => (
              <tr key={rowIndex} className="hover:bg-[var(--surface-muted)]/80">
                {row.map((cell, cellIndex) => (
                  <td key={cellIndex} className="px-4 py-3 align-top text-[var(--foreground)]">
                    {cell}
                  </td>
                ))}
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan={headers.length} className="px-4 py-8 text-center text-sm text-[var(--muted)]">
                Chua co du lieu.
              </td>
            </tr>
          )}
        </tbody>
      </table>
    </div>
  );
}
