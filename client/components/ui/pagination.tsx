"use client";

import { useMemo } from "react";
import { Button } from "@/components/ui/button";
import { SelectInput } from "@/components/ui/field";
import { classNames } from "@/lib/format";

type PaginationProps = {
  page: number;
  totalPages: number;
  onPageChange: (nextPage: number) => void;
  size?: number;
  onSizeChange?: (size: number) => void;
};

export function Pagination({ page, totalPages, onPageChange, size, onSizeChange }: PaginationProps) {
  const canPrevious = page > 0;
  const canNext = page + 1 < totalPages;

  const pageNumbers = useMemo(() => {
    if (totalPages <= 1) {
      return [1];
    }

    const current = page + 1;
    const pages = new Set<number>();
    pages.add(1);
    pages.add(totalPages);

    for (let i = current - 2; i <= current + 2; i += 1) {
      if (i >= 1 && i <= totalPages) {
        pages.add(i);
      }
    }

    return Array.from(pages).sort((a, b) => a - b);
  }, [page, totalPages]);

  const showDotsAfter = (index: number): boolean => {
    if (index >= pageNumbers.length - 1) {
      return false;
    }
    return pageNumbers[index + 1] - pageNumbers[index] > 1;
  };

  return (
    <div className="flex flex-wrap items-center justify-between gap-3">
      <div className="flex items-center gap-2 text-sm text-[var(--muted)]">
        <span>
          Trang <span className="font-semibold text-[var(--foreground)]">{totalPages === 0 ? 0 : page + 1}</span> / {totalPages}
        </span>
        {onSizeChange ? (
          <div className="flex items-center gap-2">
            <span>/ Mỗi trang</span>
            <SelectInput
              value={size ?? 10}
              onChange={(event) => onSizeChange(Number(event.target.value))}
              className="w-20 px-2 py-1.5 text-sm"
            >
              <option value={10}>10</option>
              <option value={20}>20</option>
              <option value={50}>50</option>
            </SelectInput>
          </div>
        ) : null}
      </div>

      <div className="flex flex-wrap items-center gap-2">
        <Button variant="secondary" disabled={!canPrevious} onClick={() => onPageChange(page - 1)}>
          Trước
        </Button>

        <div className="flex items-center gap-1">
          {pageNumbers.map((pageNumber, index) => {
            const isActive = pageNumber === page + 1;
            return (
              <div key={`page-${pageNumber}`} className="flex items-center gap-1">
                <button
                  type="button"
                  className={classNames(
                    "h-9 min-w-9 rounded-lg border px-2 text-sm font-semibold transition",
                    isActive
                      ? "border-[var(--primary)] bg-[color:rgba(14,122,191,0.12)] text-[var(--primary)]"
                      : "border-[var(--border)] bg-white text-[var(--foreground)] hover:bg-[var(--surface-muted)]",
                  )}
                  onClick={() => onPageChange(pageNumber - 1)}
                >
                  {pageNumber}
                </button>
                {showDotsAfter(index) ? <span className="px-1 text-[var(--muted)]">…</span> : null}
              </div>
            );
          })}
        </div>

        <Button variant="secondary" disabled={!canNext} onClick={() => onPageChange(page + 1)}>
          Sau
        </Button>
      </div>
    </div>
  );
}
