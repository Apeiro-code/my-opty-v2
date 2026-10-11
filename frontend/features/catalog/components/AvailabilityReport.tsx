"use client";

import { useCallback, useEffect, useState } from "react";
import { getAvailabilityReport } from "../api";
import type { ApiResult } from "@/types/api";
import type {
  AvailabilityItem,
  AvailabilityReport as Report,
  StockStatus,
} from "../types";

const REFRESH_INTERVAL_MS = 30_000;

const STATUS_LABEL: Record<StockStatus, string> = {
  IN_STOCK: "In stock",
  LOW_STOCK: "Low stock",
  OUT_OF_STOCK: "Out of stock",
};

const STATUS_BADGE: Record<StockStatus, string> = {
  IN_STOCK: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-400",
  LOW_STOCK: "bg-amber-500/15 text-amber-700 dark:text-amber-400",
  OUT_OF_STOCK: "bg-red-500/15 text-red-700 dark:text-red-400",
};

/** Out of stock first, then low, so the shelves to restock are at the top. */
const STATUS_ORDER: Record<StockStatus, number> = {
  OUT_OF_STOCK: 0,
  LOW_STOCK: 1,
  IN_STOCK: 2,
};

/**
 * The shop owner's live availability report: every active frame and lens with
 * its current stock and health band, so reorders can be planned against what is
 * actually on the shelf.
 *
 * <p>A client component because it polls. The backend computes the report on
 * read, so a refresh — on a button, or on a light interval — is what keeps it
 * real-time. The backend is also the authority on the role boundary; this only
 * reports what it says, turning a 403/401 into "sign in as the shop owner".
 */
export default function AvailabilityReport() {
  const [report, setReport] = useState<Report | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [refreshError, setRefreshError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const apply = useCallback((result: ApiResult<Report>) => {
    if (!result.ok) {
      const message = messageFor(result.error);
      setRefreshError(message);
      // Keep the last good report visible; surface the error beside it.
      setLoadError((current) => current ?? message);
      return;
    }
    setReport(result.data);
    setRefreshError(null);
    setLoadError(null);
  }, []);

  useEffect(() => {
    let active = true;
    const tick = () => {
      getAvailabilityReport().then((result) => {
        if (active) {
          apply(result);
        }
      });
    };
    tick();
    const timer = setInterval(tick, REFRESH_INTERVAL_MS);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, [apply]);

  async function onRefresh() {
    setRefreshing(true);
    apply(await getAvailabilityReport());
    setRefreshing(false);
  }

  if (report === null) {
    if (loadError) {
      return (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {loadError}
        </p>
      );
    }
    return <p className="text-sm opacity-70">Loading…</p>;
  }

  const items = [...report.items].sort(
    (a, b) =>
      STATUS_ORDER[a.status] - STATUS_ORDER[b.status] ||
      a.name.localeCompare(b.name),
  );

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <dl className="flex flex-wrap gap-3">
          <Summary label="Items" value={report.totalItems} />
          <Summary label="In stock" value={report.inStockCount} />
          <Summary label="Low stock" value={report.lowStockCount} />
          <Summary label="Out of stock" value={report.outOfStockCount} />
        </dl>
        <button
          type="button"
          onClick={() => void onRefresh()}
          disabled={refreshing}
          className="rounded-md border border-black/15 px-3 py-1.5 text-sm disabled:opacity-50 dark:border-white/20"
        >
          {refreshing ? "Refreshing…" : "Refresh"}
        </button>
      </div>

      {refreshError ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {refreshError}
        </p>
      ) : null}

      {items.length === 0 ? (
        <p className="text-sm opacity-70">
          No active frames or lenses to report yet.
        </p>
      ) : (
        <ul className="flex flex-col gap-2">
          {items.map((item) => (
            <AvailabilityRow
              key={`${item.itemType}-${item.itemId}`}
              item={item}
            />
          ))}
        </ul>
      )}
    </div>
  );
}

function AvailabilityRow({ item }: { item: AvailabilityItem }) {
  return (
    <li className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-black/10 p-3 text-sm dark:border-white/15">
      <div className="flex flex-wrap items-center gap-2">
        <span className="font-medium">{item.name}</span>
        <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
          {item.itemType === "FRAME" ? "Frame" : "Lens"}
        </span>
        <span
          className={`rounded px-2 py-0.5 text-xs ${STATUS_BADGE[item.status]}`}
        >
          {STATUS_LABEL[item.status]}
        </span>
      </div>
      <span className="opacity-80">
        {item.stockQty} in stock
        <span className="opacity-60"> · low at {item.lowStockThreshold}</span>
      </span>
    </li>
  );
}

function Summary({ label, value }: { label: string; value: number }) {
  return (
    <div className="rounded-lg border border-black/10 px-3 py-2 dark:border-white/15">
      <dt className="text-xs opacity-70">{label}</dt>
      <dd className="text-lg font-semibold">{value}</dd>
    </div>
  );
}

function messageFor(error: { code: string; message: string }): string {
  return error.code === "FORBIDDEN" || error.code === "UNAUTHENTICATED"
    ? "Sign in as the shop owner to see availability."
    : error.message;
}
