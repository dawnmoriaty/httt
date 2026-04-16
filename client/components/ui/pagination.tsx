"use client";

import { Button } from "@/components/ui/button";

type PaginationProps = {
  page: number;
  totalPages: number;
  onPageChange: (nextPage: number) => void;
};

export function Pagination({ page, totalPages, onPageChange }: PaginationProps) {
  const canPrevious = page > 0;
  const canNext = page + 1 < totalPages;

  return (
    <div className="flex items-center justify-between gap-3">
      <p className="text-sm text-[var(--muted)]">
        Trang <span className="font-semibold text-[var(--foreground)]">{totalPages === 0 ? 0 : page + 1}</span> / {totalPages}
      </p>
      <div className="flex items-center gap-2">
        <Button variant="secondary" disabled={!canPrevious} onClick={() => onPageChange(page - 1)}>
          Truoc
        </Button>
        <Button variant="secondary" disabled={!canNext} onClick={() => onPageChange(page + 1)}>
          Sau
        </Button>
      </div>
    </div>
  );
}
