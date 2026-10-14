"use client";

import { useEffect, useState } from "react";
import { listFaq } from "../api";
import type { Faq } from "../types";

/**
 * The public FAQ: answered questions the shop marked as generally useful.
 *
 * <p>Readable without signing in — the endpoint is public, and a visitor
 * weighing up the shop should be able to read it before creating an account. A
 * client component because it loads; a fetch failure is shown as text rather
 * than left blank so an empty list and a broken one are distinguishable.
 */
export default function FaqList() {
  const [items, setItems] = useState<Faq[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listFaq().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(result.error.message);
        return;
      }
      setLoadError(null);
      setItems(result.data);
    });
    return () => {
      active = false;
    };
  }, []);

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
        No frequently asked questions yet. Ask below and the shop&apos;s answer
        may appear here.
      </p>
    );
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((faq) => (
        <li
          key={faq.id}
          className="rounded-lg border border-black/10 p-4 text-sm dark:border-white/15"
        >
          <p className="font-medium">{faq.questionText}</p>
          <p className="mt-2 opacity-80">{faq.answerText}</p>
        </li>
      ))}
    </ul>
  );
}
