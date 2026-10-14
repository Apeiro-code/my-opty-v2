"use client";

import { useEffect, useState } from "react";
import { getLowStockAlerts } from "../api";
import { STATUS_BADGE, STATUS_LABEL } from "../statusDisplay";
import type { AvailabilityItem } from "../types";

const REFRESH_INTERVAL_MS = 30_000;

/**
 * The in-app half of the low-stock alert: a banner listing the frames and lenses
 * at or below their threshold, so the shop sees what to reorder without waiting
 * for the next email.
 *
 * <p>A client component because it polls. An empty list renders nothing — a
 * healthy shop has no banner — which is why this sits above the availability
 * report rather than replacing it: the banner is only the urgent slice, the
 * report below is everything.
 *
 * <p>A load that fails renders nothing here rather than a second error: the
 * availability report right below is the authoritative view and already surfaces
 * the same auth/network failure, so repeating it would only be noise.
 */
export default function LowStockAlerts() {
  const [items, setItems] = useState<AvailabilityItem[]>([]);

  useEffect(() => {
    let active = true;
    const tick = () => {
      getLowStockAlerts().then((result) => {
        if (active && result.ok) {
          setItems(result.data);
        }
      });
    };
    tick();
    const timer = setInterval(tick, REFRESH_INTERVAL_MS);
    return () => {
      active = false;
      clearInterval(timer);
    };
  }, []);

  if (items.length === 0) {
    return null;
  }

  return (
    <div
      role="status"
      className="rounded-lg border border-amber-500/40 bg-amber-500/10 p-4"
    >
      <p className="font-medium text-amber-800 dark:text-amber-300">
        {items.length} {items.length === 1 ? "item needs" : "items need"}{" "}
        restocking
      </p>
      <ul className="mt-2 flex flex-col gap-1.5 text-sm">
        {items.map((item) => (
          <li
            key={`${item.itemType}-${item.itemId}`}
            className="flex flex-wrap items-center gap-2"
          >
            <span className="font-medium">{item.name}</span>
            <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
              {item.itemType === "FRAME" ? "Frame" : "Lens"}
            </span>
            <span
              className={`rounded px-2 py-0.5 text-xs ${STATUS_BADGE[item.status]}`}
            >
              {STATUS_LABEL[item.status]}
            </span>
            <span className="opacity-80">
              {item.stockQty} in stock
              <span className="opacity-60">
                {" "}
                · low at {item.lowStockThreshold}
              </span>
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}
