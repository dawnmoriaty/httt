export function toVietnameseStatus(status: number): string {
  if (status === 1) {
    return "Hoat dong";
  }

  if (status === 2) {
    return "Ngung hoat dong";
  }

  return `Status ${status}`;
}

export function classNames(...values: Array<string | false | null | undefined>): string {
  return values.filter(Boolean).join(" ");
}
