"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useAuth } from "@/lib/auth/AuthContext";

/**
 * The header's session controls: a way in when signed out, and the identity plus
 * a way out when signed in.
 *
 * <p>A client component because it reads the session context and its sign-out
 * button is an event handler. It is the same widget in both layouts — the
 * storefront and the shop — so a shop owner can end a session from wherever they
 * happen to be.
 */
export default function AuthNav() {
  const { user, loading, logout } = useAuth();
  const router = useRouter();
  const [signingOut, setSigningOut] = useState(false);

  if (loading) {
    return <span className="ml-auto text-sm opacity-50">…</span>;
  }

  if (!user) {
    return (
      <Link href="/login" className="ml-auto text-sm hover:underline">
        Log in
      </Link>
    );
  }

  async function onLogout() {
    setSigningOut(true);
    await logout();
    router.replace("/");
    router.refresh();
  }

  return (
    <span className="ml-auto flex items-center gap-3 text-sm">
      <Link href="/account" className="hover:underline">
        {user.fullName}
      </Link>
      <button
        type="button"
        onClick={onLogout}
        disabled={signingOut}
        className="rounded-md border border-black/20 px-3 py-1 text-xs hover:bg-black/5 disabled:opacity-50 dark:border-white/25 dark:hover:bg-white/10"
      >
        {signingOut ? "Signing out…" : "Log out"}
      </button>
    </span>
  );
}
