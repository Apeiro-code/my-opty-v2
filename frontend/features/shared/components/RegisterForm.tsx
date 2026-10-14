"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { useAuth } from "@/lib/auth/AuthContext";
import type { Role } from "@/lib/auth/types";
import type { ApiError } from "@/types/api";

/**
 * The customer's way in: a registration form that establishes the account and
 * then hands the visitor to the page they came for.
 *
 * <p>A client component because the whole thing is state and an event handler.
 * The backend is the authority on the credentials; the only client-side rule
 * here is where to send the visitor afterwards — a `CLIENT` belongs in the shop,
 * a `CUSTOMER` on their account page.
 *
 * <p>This form matches the backend {@link RegisterRequest} exactly:
 * name (min 2 chars), email (valid email), phone (optional), password (min 5 chars).
 */
export default function RegisterForm({ next }: { next?: string }) {
  const { register } = useAuth();
  const router = useRouter();
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const user = await register(name, email, phone, password);
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
        <label htmlFor="name" className="text-sm font-medium">
          Name
        </label>
        <input
          id="name"
          name="name"
          required
          minLength={2}
          maxLength={100}
          value={name}
          onChange={(event) => setName(event.target.value)}
          className="rounded-md border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
        />
      </div>

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
        <label htmlFor="phone" className="text-sm font-medium">
          Phone
        </label>
        <input
          id="phone"
          name="phone"
          value={phone}
          onChange={(event) => setPhone(event.target.value)}
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
          required
          minLength={5}
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
        {submitting ? "Registering…" : "Register"}
      </button>
    </form>
  );
}

/**
 * Where to land after a successful registration. A shop owner goes to the shop;
 * a customer goes back to the page they came for, or to their account. `next` is
 * honoured only when it is a path on this site — an absolute URL or a
 * protocol-relative `//host` would turn a register link into an open redirect.
 */
function destinationFor(role: Role, next?: string): string {
  if (role === "CLIENT") {
    return "/shop/orders";
  }
  if (next && next.startsWith("/") && !next.startsWith("//")) {
    return next;
  }
  return "/login";
}

function messageFor(caught: unknown): string {
  const error = caught as Partial<ApiError> | undefined;
  switch (error?.code) {
    case "EMAIL_ALREADY_EXISTS":
      return "An account with this email already exists. Please log in or use a different email.";
    case "VALIDATION_FAILED":
      return "Please fill in all required fields correctly.";
    case "WEAK_PASSWORD":
      return "Password should be at least 5 characters.";
    default:
      return error?.message ?? "Could not register. Please try again.";
  }
}
