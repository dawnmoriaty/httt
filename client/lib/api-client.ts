import { clearTokens, getTokens, saveTokens } from "@/lib/auth-store";
import type { ApiErrorResponse, ApiResponse, AuthResponse } from "@/lib/types";

const API_BASE_URL = "/api";

let refreshInFlight: Promise<string | null> | null = null;

function resolvePath(path: string): string {
  if (path.startsWith("http://") || path.startsWith("https://")) {
    return path;
  }

  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  return `${API_BASE_URL}${normalizedPath}`;
}

function toErrorBody(payload: unknown): ApiErrorResponse {
  if (payload && typeof payload === "object") {
    const asRecord = payload as Record<string, unknown>;
    return {
      errorCode: typeof asRecord.errorCode === "string" ? asRecord.errorCode : "UNKNOWN_ERROR",
      message: typeof asRecord.message === "string" ? asRecord.message : "Co loi xay ra.",
      fieldErrors:
        asRecord.fieldErrors && typeof asRecord.fieldErrors === "object"
          ? (asRecord.fieldErrors as Record<string, string>)
          : null,
    };
  }

  return {
    errorCode: "UNKNOWN_ERROR",
    message: "Co loi xay ra.",
    fieldErrors: null,
  };
}

export class ApiClientError extends Error {
  status: number;
  errorCode: string;
  fieldErrors: Record<string, string> | null;

  constructor(status: number, payload: ApiErrorResponse) {
    super(payload.message);
    this.status = status;
    this.errorCode = payload.errorCode;
    this.fieldErrors = payload.fieldErrors ?? null;
  }
}

async function refreshAccessToken(): Promise<string | null> {
  const tokens = getTokens();
  if (!tokens?.refreshToken) {
    return null;
  }

  try {
    const response = await fetch(resolvePath("/auth/refresh"), {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify({ refreshToken: tokens.refreshToken }),
    });

    if (!response.ok) {
      clearTokens();
      return null;
    }

    const payload = (await response.json()) as ApiResponse<AuthResponse>;
    const authResponse = payload.data;
    saveTokens({
      accessToken: authResponse.accessToken,
      refreshToken: authResponse.refreshToken,
    });

    return authResponse.accessToken;
  } catch {
    clearTokens();
    return null;
  }
}

async function ensureRefreshedToken(): Promise<string | null> {
  if (!refreshInFlight) {
    refreshInFlight = refreshAccessToken().finally(() => {
      refreshInFlight = null;
    });
  }

  return refreshInFlight;
}

type RequestOptions = {
  method?: "GET" | "POST" | "PUT" | "DELETE";
  body?: unknown;
  headers?: Record<string, string>;
  auth?: boolean;
  retryOnUnauthorized?: boolean;
};

export function buildPagingQuery(params: { page: number; size: number; q?: string }): string {
  const searchParams = new URLSearchParams();
  searchParams.set("page", String(params.page));
  searchParams.set("size", String(params.size));
  if (params.q && params.q.trim().length > 0) {
    searchParams.set("q", params.q.trim());
  }

  return searchParams.toString();
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const {
    method = "GET",
    body,
    headers = {},
    auth = true,
    retryOnUnauthorized = true,
  } = options;

  const tokens = getTokens();
  const requestHeaders: Record<string, string> = {
    ...headers,
  };

  if (body !== undefined) {
    requestHeaders["Content-Type"] = "application/json";
  }

  if (auth && tokens?.accessToken) {
    requestHeaders.Authorization = `Bearer ${tokens.accessToken}`;
  }

  const response = await fetch(resolvePath(path), {
    method,
    headers: requestHeaders,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  if (response.status === 204) {
    return undefined as T;
  }

  if (response.status === 401 && auth && retryOnUnauthorized) {
    const newAccessToken = await ensureRefreshedToken();
    if (!newAccessToken) {
      throw new ApiClientError(401, {
        errorCode: "UNAUTHORIZED",
        message: "Phien dang nhap da het han. Vui long dang nhap lai.",
        fieldErrors: null,
      });
    }

    return apiRequest<T>(path, {
      ...options,
      retryOnUnauthorized: false,
    });
  }

  if (!response.ok) {
    let payload: unknown = null;
    try {
      payload = await response.json();
    } catch {
      payload = null;
    }
    throw new ApiClientError(response.status, toErrorBody(payload));
  }

  const payload = (await response.json()) as ApiResponse<T>;
  return payload.data;
}
