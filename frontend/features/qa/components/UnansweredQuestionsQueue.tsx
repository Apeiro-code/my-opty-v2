"use client";

import { useEffect, useState } from "react";
import { answerQuestion, listUnansweredQuestions } from "../api";
import type { ShopQuestion } from "../types";

/**
 * The shop owner's answering queue: every open question with who asked it, so the
 * owner can write a reply. Posting the answer emails the customer and removes the
 * question from the queue.
 *
 * <p>A client component because the whole thing is state and handlers. The load
 * and the answer both go through `features/qa/api`, so the session cookie and the
 * error envelope are handled the same way as the rest of the app.
 */
export default function UnansweredQuestionsQueue() {
  const [items, setItems] = useState<ShopQuestion[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [answeringId, setAnsweringId] = useState<number | null>(null);
  const [draft, setDraft] = useState("");

  useEffect(() => {
    let active = true;
    listUnansweredQuestions().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "FORBIDDEN" ||
            result.error.code === "UNAUTHENTICATED"
            ? "Sign in as the shop owner to answer questions."
            : result.error.message,
        );
        return;
      }
      setLoadError(null);
      setItems(result.data);
    });
    return () => {
      active = false;
    };
  }, []);

  async function onAnswer(id: number) {
    const trimmed = draft.trim();
    if (!trimmed) {
      setError("Write an answer before posting.");
      return;
    }
    setError(null);
    setBusyId(id);
    const result = await answerQuestion(id, trimmed);
    setBusyId(null);
    if (!result.ok) {
      setError(
        result.error.code === "ALREADY_ANSWERED"
          ? "That question has already been answered."
          : result.error.message,
      );
      return;
    }
    setAnsweringId(null);
    setDraft("");
    setItems((current) => current?.filter((item) => item.id !== id) ?? null);
  }

  if (loadError) {
    return (
      <p role="alert" className="text-sm text-red-600 dark:text-red-400">
        {loadError}
      </p>
    );
  }

  if (items === null) {
    return <p className="text-sm opacity-70">Loading…</p>;
  }

  if (items.length === 0) {
    return (
      <p className="text-sm opacity-70">
        No questions are waiting for an answer right now.
      </p>
    );
  }

  return (
    <div className="flex flex-col gap-4">
      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      <ul className="flex flex-col gap-4">
        {items.map((question) => (
          <li
            key={question.id}
            className="rounded-lg border border-black/10 p-4 dark:border-white/15"
          >
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <span className="font-medium">
                {question.customerName ?? "A customer"}
              </span>
              {question.customerEmail ? (
                <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                  {question.customerEmail}
                </span>
              ) : null}
              <span className="text-xs opacity-70">
                Asked {formatDate(question.askedAt)}
              </span>
            </div>
            <p className="mt-3 opacity-90">{question.questionText}</p>

            {answeringId === question.id ? (
              <div className="mt-4 flex flex-col gap-2">
                <label
                  htmlFor={`question-answer-${question.id}`}
                  className="text-sm font-medium"
                >
                  Your answer
                </label>
                <textarea
                  id={`question-answer-${question.id}`}
                  value={draft}
                  onChange={(event) => setDraft(event.target.value)}
                  rows={4}
                  placeholder="Write the answer the customer will receive by email."
                  className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
                />
                <div className="flex gap-2">
                  <button
                    type="button"
                    disabled={busyId === question.id}
                    onClick={() => void onAnswer(question.id)}
                    className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                  >
                    Post answer
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setAnsweringId(null);
                      setDraft("");
                      setError(null);
                    }}
                    className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            ) : (
              <div className="mt-4">
                <button
                  type="button"
                  onClick={() => {
                    setAnsweringId(question.id);
                    setDraft("");
                    setError(null);
                  }}
                  className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                >
                  Answer…
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}
