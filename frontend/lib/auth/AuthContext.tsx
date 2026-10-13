"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
} from "./api";
import type { AuthenticatedUser } from "./types";

/**
 * The one place the browser knows who is signed in.
 *
 * <p>The session itself lives on the API, not here: this context only caches the
 * answer to `/api/auth/me` so the header and the account page do not each fetch
 * it. A 401 is an expected answer for an anonymous visitor and resolves to
 * `user = null`, not to an error — an anonymous storefront is the normal case,
 * not a failure.
 *
 * <p>`login` goes through the API first and only moves this state once the
 * server has agreed, so the UI can never claim a session the API did not create.
 * `logout` is best-effort: the local state is cleared whether or not the call
 * lands, so a network hiccup cannot strand a visitor in a session they asked to
 * leave; the cookie is httpOnly and the server still ends it on the next call.
 */
type AuthContextValue = {
  user: AuthenticatedUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<AuthenticatedUser>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthenticatedUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    const result = await getCurrentUser();
    setUser(result.ok ? result.data : null);
  }, []);

  useEffect(() => {
    let active = true;
    getCurrentUser()
      .then((result) => {
        if (!active) return;
        setUser(result.ok ? result.data : null);
        setLoading(false);
      })
      .catch(() => {
        if (!active) return;
        setUser(null);
        setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const result = await loginRequest(email, password);
    if (!result.ok) {
      throw result.error;
    }
    setUser(result.data);
    return result.data;
  }, []);

  const logout = useCallback(async () => {
    await logoutRequest();
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, logout, refresh }),
    [user, loading, login, logout, refresh],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used inside an AuthProvider");
  }
  return context;
}
