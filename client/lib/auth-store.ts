import type { Tokens } from "@/lib/types";

const ACCESS_TOKEN_KEY = "httt_access_token";
const REFRESH_TOKEN_KEY = "httt_refresh_token";

function isBrowser(): boolean {
  return typeof window !== "undefined";
}

export function getTokens(): Tokens | null {
  if (!isBrowser()) {
    return null;
  }

  const accessToken = window.localStorage.getItem(ACCESS_TOKEN_KEY);
  const refreshToken = window.localStorage.getItem(REFRESH_TOKEN_KEY);
  if (!accessToken || !refreshToken) {
    return null;
  }

  return { accessToken, refreshToken };
}

export function saveTokens(tokens: Tokens): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  window.localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
}

export function clearTokens(): void {
  if (!isBrowser()) {
    return;
  }

  window.localStorage.removeItem(ACCESS_TOKEN_KEY);
  window.localStorage.removeItem(REFRESH_TOKEN_KEY);
}
