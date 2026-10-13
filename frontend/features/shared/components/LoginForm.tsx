"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { useAuth } from "@/lib/auth/AuthContext";
import type { Role } from "@/lib/auth/types";
import type { ApiError } from "@/types/api";

/**
 * The customer's way in: an email/password form that establishes the session and
 * then hands the visitor to the page they came for.
 *
 * <p>A client component because the whole thing is state and an event handler.
 * The backend is the authority on the credentials; the only client-side rule
 * here is where to send the visitor afterwards — a `CLIENT` belongs in the shop,
 * a `CUSTOMER` on their account page.
 */
export default function LoginForm({ next }: { next?: string }) {
  const { login } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await login(email, password);
      router.replace(destinationFor(user.role, next));
      router.refresh();
    } catch (caught) {
      setSubmitting(false);
      setError(messageFor(caught));
    }
  }

  return (
    <form onSubmit={onSubmit} className="flex max-w-md flex-col gap-4">
      <div className="flex flex-col gap-1">
        <label htmlFor="email" className="text-sm font-medium">
          Email
        </label>
        <input
          id="email"
          name="email"
          type="email"
          autoComplete="email"
          required
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          className="rounded-md border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
        />
      </div>

      <div className="flex flex-col gap-1">
        <label htmlFor="password" className="text-sm font-medium">
          Password
        </label>
        <input
          id="password"
          name="password"
          type="password"
          autoComplete="current-password"
          required
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          className="rounded-md border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
        />
      </div>

      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={submitting}
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-50"
      >
        {submitting ? "Signing in…" : "Sign in"}
      </button>
    </form>
  );
}

/**
 * Where to land after a successful login. A shop owner goes to the shop; a
 * customer goes back to the page they came for, or to their account. `next` is
 * honoured only when it is a path on this site — an absolute URL or a
 * protocol-relative `//host` would turn a login link into an open redirect.
 */
function destinationFor(role: Role, next?: string): string {
  if (role === "CLIENT") {
    return "/shop/orders";
  }
  if (next && next.startsWith("/") && !next.startsWith("//")) {
    return next;
  }
  return "/account";
}

function messageFor(caught: unknown): string {
  const error = caught as Partial<ApiError> | undefined;
  switch (error?.code) {
    case "INVALID_CREDENTIALS":
      return "Email or password is incorrect.";
    case "ACCOUNT_NOT_ACTIVE":
      return "This account is not active. Check your email for a verification link.";
    case "VALIDATION_FAILED":
      return "Enter both your email and password.";
    default:
      return error?.message ?? "Could not sign in. Please try again.";
  }
}
