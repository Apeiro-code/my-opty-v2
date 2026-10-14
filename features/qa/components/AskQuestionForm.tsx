"use client";

import { useState, type FormEvent } from "react";
import { askQuestion } from "../api";
import type { ApiError } from "@/types/api";

const MAX_LENGTH = 2000;

/**
 * The customer's question box. On success it clears the text and tells the
 * parent to reload the list, so the new question appears immediately.
 *
 * <p>A client component because the whole thing is form state. It enforces the
 * length the backend also enforces so an over-long question fails instantly, but
 * the server remains the authority.
 */
export default function AskQuestionForm({ onAsked }: { onAsked: () => void }) {
  const [text, setText] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const questionText = text.trim();
    if (questionText === "") {
      setError("Type your question first.");
      return;
    }

    setSubmitting(true);
    try {
      const result = await askQuestion({ questionText });
      if (!result.ok) {
        setError(messageFor(result.error));
        return;
      }
      setText("");
      onAsked();
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form onSubmit={onSubmit} className="flex max-w-2xl flex-col gap-3">
      <label htmlFor="question-text" className="text-sm font-medium">
        Ask the shop a question
      </label>
      <textarea
        id="question-text"
        name="questionText"
        rows={3}
        maxLength={MAX_LENGTH}
        required
        value={text}
        onChange={(event) => setText(event.target.value)}
        placeholder="e.g. Do you offer blue-light filtering lenses?"
        className="rounded-md border border-black/15 bg-transparent px-3 py-2 text-sm dark:border-white/20"
      />

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
        {submitting ? "Sending…" : "Send question"}
      </button>
    </form>
  );
}

function messageFor(error: ApiError): string {
  switch (error.code) {
    case "VALIDATION_FAILED":
      return "Type your question first.";
    case "UNAUTHENTICATED":
      return "Your session has ended. Please sign in again.";
    default:
      return error.message ?? "Could not send your question. Please try again.";
  }
}
