"use client";

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { apiRequest } from "@/lib/api-client";
import { clearTokens, getTokens, saveTokens } from "@/lib/auth-store";
import type { AuthResponse, CurrentUser } from "@/lib/types";

type AuthContextValue = {
  user: CurrentUser | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (payload: { username: string; fullName: string; email: string; password: string }) => Promise<void>;
  logout: () => Promise<void>;
  hasPermission: (resource: string, action: string) => boolean;
  reloadMe: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function normalizePermission(resource: string, action: string): string {
  return `${resource.trim().toLowerCase()}:${action.trim().toUpperCase()}`;
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  const loadMe = useCallback(async () => {
    const tokens = getTokens();
    if (!tokens) {
      setUser(null);
      return;
    }

    const me = await apiRequest<CurrentUser>("/auth/me");
    setUser(me);
  }, []);

  useEffect(() => {
    const run = async () => {
      try {
        await loadMe();
      } catch {
        clearTokens();
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    void run();
  }, [loadMe]);

  const login = useCallback(async (username: string, password: string) => {
    const response = await apiRequest<AuthResponse>("/auth/login", {
      method: "POST",
      auth: false,
      body: { username, password },
    });

    saveTokens({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    });
    setUser(response.user);
  }, []);

  const register = useCallback(async (payload: { username: string; fullName: string; email: string; password: string }) => {
    const response = await apiRequest<AuthResponse>("/auth/register", {
      method: "POST",
      auth: false,
      body: payload,
    });

    saveTokens({
      accessToken: response.accessToken,
      refreshToken: response.refreshToken,
    });
    setUser(response.user);
  }, []);

  const logout = useCallback(async () => {
    const tokens = getTokens();
    try {
      if (tokens?.accessToken) {
        await apiRequest<void>("/auth/logout", {
          method: "POST",
          headers: {
            Authorization: `Bearer ${tokens.accessToken}`,
          },
        });
      }
    } finally {
      clearTokens();
      setUser(null);
    }
  }, []);

  const hasPermission = useCallback(
    (resource: string, action: string): boolean => {
      if (!user) {
        return false;
      }

      const expected = normalizePermission(resource, action);
      return user.permissions.includes(expected);
    },
    [user],
  );

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      loading,
      isAuthenticated: !!user,
      login,
      register,
      logout,
      hasPermission,
      reloadMe: loadMe,
    }),
    [user, loading, login, register, logout, hasPermission, loadMe],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
