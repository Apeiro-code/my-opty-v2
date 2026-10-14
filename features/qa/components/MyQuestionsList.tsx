"use client";

import { useEffect, useState } from "react";
import { listOwnQuestions } from "../api";
import type { Question } from "../types";

/**
 * The signed-in customer's own questions, newest first, each with the shop's
 * answer once there is one.
 *
 * <p>`refreshKey` is bumped by the ask form after a successful submission; the
 * list reloads on the change, so a new question appears without a page refresh.
 * The backend scopes the read to the session, so this component passes no id.
 */
export default function MyQuestionsList({
  refreshKey,
}: {
  refreshKey: number;
}) {
  const [items, setItems] = useState<Question[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listOwnQuestions().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "UNAUTHENTICATED" ||
            result.error.code === "FORBIDDEN"
            ? "Please sign in to see your questions."
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
  }, [refreshKey]);

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
        You have not asked anything yet. Use the form above and it will appear
        here.
      </p>
    );
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((question) => (
        <li
          key={question.id}
          className="rounded-lg border border-black/10 p-4 text-sm dark:border-white/15"
        >
          <div className="flex flex-wrap items-center gap-2">
            <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
              {question.answered ? "Answered" : "Waiting for the shop"}
            </span>
            <span className="text-xs opacity-70">
              Asked {formatDate(question.askedAt)}
            </span>
          </div>
          <p className="mt-2 font-medium">{question.questionText}</p>
          {question.answered && question.answerText ? (
            <div className="mt-3 border-l-2 border-black/15 pl-3 dark:border-white/20">
              <p className="text-xs opacity-70">The shop answered:</p>
              <p className="mt-1 opacity-80">{question.answerText}</p>
            </div>
          ) : (
            <p className="mt-2 text-xs opacity-70">
              We have passed this to the shop.
            </p>
          )}
        </li>
      ))}
    </ul>
  );
}

function formatDate(value: string): string {
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString();
}
