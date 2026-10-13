"use client";

import { useAuth } from "@/lib/auth/AuthContext";
import ProfileForm from "./ProfileForm";

/**
 * The account page's content: who the session says you are, and the form that
 * edits those details.
 *
 * <p>The form is the whole point of this story; the heading above it names the
 * account so the visitor knows whose profile they are editing. A `CLIENT` sees
 * the same form but the backend refuses the save — the account page is a
 * customer's, and the shop's own profile is a different story.
 */
export default function AccountPanel() {
  const { user } = useAuth();

  if (!user) {
    return null;
  }

  return (
    <section className="flex flex-col gap-4">
      <h2 className="text-lg font-medium">Profile details</h2>
      <p className="max-w-prose text-sm opacity-80">
        Keep your name, phone number and address up to date so the shop can
        reach you about an order. Your email is the address you sign in with and
        cannot be changed here.
      </p>
      <ProfileForm />
    </section>
  );
}
