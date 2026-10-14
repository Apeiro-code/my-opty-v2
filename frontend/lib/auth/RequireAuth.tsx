"use client";

import { usePathname, useRouter } from "next/navigation";
import { useEffect, type ReactNode } from "react";
import { useAuth } from "./AuthContext";

/**
 * Keeps a signed-out visitor out of a page that needs a session.
 *
 * <p>It waits for the session check to settle before deciding — redirecting
 * during `loading` would bounce a signed-in customer back to the login page on
 * every refresh, because `user` is momentarily null while `/api/auth/me` is in
 * flight. The target is captured as `?next=` so login can send the visitor back
 * where they were headed.
 *
 * <p>This is the fast half of the rule, not the authority: the API still refuses
 * every protected call with a 401. Its job is to spare the visitor a page full
 * of failed requests.
 */
export default function RequireAuth({ children }: { children: ReactNode }) {
  const { user, loading } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (!loading && !user) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
    }
  }, [loading, user, router, pathname]);

  if (loading || !user) {
    return (
      <p className="py-8 text-sm opacity-70" role="status">
        Checking your session…
      </p>
    );
  }

  return <>{children}</>;
}
