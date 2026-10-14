"use client";

import { useCallback, useEffect, useState } from "react";
import { exportAvailabilityReport, getAvailabilityReport } from "../api";
import type { ApiResult } from "@/types/api";
import { STATUS_BADGE, STATUS_LABEL, STATUS_ORDER } from "../statusDisplay";
import type { AvailabilityItem, AvailabilityReport as Report } from "../types";

const REFRESH_INTERVAL_MS = 30_000;

/**
 * The shop owner's live availability report: every active frame and lens with
 * its current stock and health band, so reorders can be planned against what is
 * actually on the shelf.
 *
 * <p>A client component because it polls. The backend computes the report on
 * read, so a refresh — on a button, or on a light interval — is what keeps it
 * real-time. The backend is also the authority on the role boundary; this only
 * reports what it says, turning a 403/401 into "sign in as the shop owner".
 *
 * <p>The export buttons hand the same rows to the backend as a file: the server
 * renders the PDF/Excel, and the browser names the download.
 */
export default function AvailabilityReport() {
  const [report, setReport] = useState<Report | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [refreshError, setRefreshError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [exporting, setExporting] = useState<"pdf" | "xlsx" | null>(null);

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

  async function onExport(format: "pdf" | "xlsx") {
    setExporting(format);
    const date = new Date().toISOString().slice(0, 10);
    const result = await exportAvailabilityReport(
      format,
      `availability-report-${date}.${format}`,
    );
    setRefreshError(result.ok ? null : messageFor(result.error));
    setExporting(null);
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
        <div className="flex flex-wrap items-center gap-2">
          <button
            type="button"
            onClick={() => void onExport("pdf")}
            disabled={exporting !== null}
            className="rounded-md border border-black/15 px-3 py-1.5 text-sm disabled:opacity-50 dark:border-white/20"
          >
            {exporting === "pdf" ? "Exporting…" : "Export PDF"}
          </button>
          <button
            type="button"
            onClick={() => void onExport("xlsx")}
            disabled={exporting !== null}
            className="rounded-md border border-black/15 px-3 py-1.5 text-sm disabled:opacity-50 dark:border-white/20"
          >
            {exporting === "xlsx" ? "Exporting…" : "Export Excel"}
          </button>
          <button
            type="button"
            onClick={() => void onRefresh()}
            disabled={refreshing}
            className="rounded-md border border-black/15 px-3 py-1.5 text-sm disabled:opacity-50 dark:border-white/20"
          >
            {refreshing ? "Refreshing…" : "Refresh"}
          </button>
        </div>
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
