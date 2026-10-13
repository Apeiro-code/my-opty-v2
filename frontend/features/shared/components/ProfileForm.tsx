"use client";

import { useState, type FormEvent, type ReactNode } from "react";
import { useAuth } from "@/lib/auth/AuthContext";
import type { PreferredContact } from "@/lib/auth/types";
import type { ApiError } from "@/types/api";

const CONTACT_OPTIONS: { value: PreferredContact; label: string }[] = [
  { value: "EMAIL", label: "Email" },
  { value: "PHONE", label: "Phone call" },
  { value: "SMS", label: "Text message (SMS)" },
];

/**
 * The customer's profile editor: name, phone, address and how they prefer to be
 * contacted. The email is shown but disabled — it is the login identity, and
 * changing it is a verification story of its own.
 *
 * <p>A client component because the whole thing is form state. On success it
 * shows the server's answer rather than the typed values, so a blank field that
 * the backend normalised to null does not read as "saved" while the stored row
 * disagrees.
 */
export default function ProfileForm() {
  const { user, updateProfile } = useAuth();
  const [name, setName] = useState(user?.fullName ?? "");
  const [phone, setPhone] = useState(user?.phone ?? "");
  const [address, setAddress] = useState(user?.address ?? "");
  const [preferredContact, setPreferredContact] = useState<
    PreferredContact | ""
  >(user?.preferredContact ?? "");
  const [error, setError] = useState<string | null>(null);
  const [saved, setSaved] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  if (!user) {
    return null;
  }

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);
    setSaved(false);
    setSubmitting(true);
    try {
      const updated = await updateProfile({
        name: name.trim(),
        phone: phone.trim() === "" ? null : phone.trim(),
        address: address.trim() === "" ? null : address.trim(),
        preferredContact: preferredContact === "" ? null : preferredContact,
      });
      setName(updated.fullName);
      setPhone(updated.phone ?? "");
      setAddress(updated.address ?? "");
      setPreferredContact(updated.preferredContact ?? "");
      setSaved(true);
    } catch (caught) {
      setError(messageFor(caught));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="flex max-w-md flex-col gap-4">
      <Field label="Name" htmlFor="profile-name">
        <input
          id="profile-name"
          name="name"
          type="text"
          required
          minLength={2}
          maxLength={100}
          value={name}
          onChange={(event) => setName(event.target.value)}
          className={inputClass}
        />
      </Field>

      <Field label="Email" htmlFor="profile-email">
        <input
          id="profile-email"
          name="email"
          type="email"
          value={user.email}
          disabled
          readOnly
          className={`${inputClass} opacity-60`}
        />
      </Field>

      <Field label="Phone" htmlFor="profile-phone">
        <input
          id="profile-phone"
          name="phone"
          type="tel"
          maxLength={32}
          value={phone}
          onChange={(event) => setPhone(event.target.value)}
          className={inputClass}
        />
      </Field>

      <Field label="Address" htmlFor="profile-address">
        <textarea
          id="profile-address"
          name="address"
          rows={3}
          maxLength={500}
          value={address}
          onChange={(event) => setAddress(event.target.value)}
          className={inputClass}
        />
      </Field>

      <Field label="Preferred contact" htmlFor="profile-preferred-contact">
        <select
          id="profile-preferred-contact"
          name="preferredContact"
          value={preferredContact}
          onChange={(event) =>
            setPreferredContact(event.target.value as PreferredContact | "")
          }
          className={inputClass}
        >
          <option value="">No preference</option>
          {CONTACT_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </Field>

      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      {saved ? (
        <p role="status" className="text-sm text-green-700 dark:text-green-400">
          Your profile has been saved.
        </p>
      ) : null}

      <button
        type="submit"
        disabled={submitting}
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-50"
      >
        {submitting ? "Saving…" : "Save changes"}
      </button>
    </form>
  );
}

const inputClass =
  "rounded-md border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20";

function Field({
  label,
  htmlFor,
  children,
}: {
  label: string;
  htmlFor: string;
  children: ReactNode;
}) {
  return (
    <div className="flex flex-col gap-1">
      <label htmlFor={htmlFor} className="text-sm font-medium">
        {label}
      </label>
      {children}
    </div>
  );
}

function messageFor(caught: unknown): string {
  const error = caught as Partial<ApiError> | undefined;
  switch (error?.code) {
    case "PHONE_EXISTS":
      return "That phone number is already in use on another account.";
    case "VALIDATION_FAILED":
      return "Please check the form: a name is required.";
    case "UNAUTHENTICATED":
      return "Your session has ended. Please sign in again.";
    default:
      return error?.message ?? "Could not save your profile. Please try again.";
  }
}
