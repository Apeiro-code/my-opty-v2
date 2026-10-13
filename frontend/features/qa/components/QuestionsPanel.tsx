"use client";

import Link from "next/link";
import { useState } from "react";
import { useAuth } from "@/lib/auth/AuthContext";
import AskQuestionForm from "./AskQuestionForm";
import MyQuestionsList from "./MyQuestionsList";

/**
 * The signed-in half of the Q&amp;A page: the ask form and the customer's own
 * questions. The FAQ above it stays public, so an anonymous visitor is not
 * bounced to login just to read — this component only asks them to sign in
 * before they can ask.
 *
 * <p>A client component because it reads the session context and owns the
 * refresh counter the form bumps so the list reloads after a new question.
 */
export default function QuestionsPanel() {
  const { user, loading } = useAuth();
  const [refreshKey, setRefreshKey] = useState(0);

  if (loading) {
    return <p className="text-sm opacity-70">Checking your session…</p>;
  }

  if (!user) {
    return (
      <p className="text-sm opacity-80">
        <Link href="/login?next=%2Fquestions" className="font-medium underline">
          Sign in
        </Link>{" "}
        to ask a question and to see the ones you have already asked.
      </p>
    );
  }

  if (user.role === "CLIENT") {
    return (
      <p className="text-sm opacity-80">
        The question box is for customers. You are signed in as the shop.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-8">
      <AskQuestionForm onAsked={() => setRefreshKey((key) => key + 1)} />

      <section className="flex flex-col gap-3">
        <h2 className="text-lg font-medium">Your questions</h2>
        <MyQuestionsList refreshKey={refreshKey} />
      </section>
    </div>
  );
}
