"use client";

import type { ReactNode } from "react";
import { useAuth } from "@/lib/auth/AuthContext";

/**
 * The account page's content: who the session says you are.
 *
 * <p>Small on purpose — editing profile details and changing a password are
 * separate stories. What this proves is the thing the logout story is about: the
 * customer reached a page that only a session can show, and it names the right
 * person.
 */
export default function AccountPanel() {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  return (
    <dl className="max-w-prose divide-y divide-black/10 rounded-lg border border-black/10 dark:divide-white/15 dark:border-white/15">
      <Row label="Name">{user.fullName}</Row>
      <Row label="Email">{user.email}</Row>
      <Row label="Account type">
        {user.role === "CLIENT" ? "Shop owner" : "Customer"}
      </Row>
    </dl>
  );
}

function Row({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div className="flex flex-wrap justify-between gap-2 px-4 py-3 text-sm">
      <dt className="opacity-70">{label}</dt>
      <dd className="font-medium">{children}</dd>
    </div>
  );
}
