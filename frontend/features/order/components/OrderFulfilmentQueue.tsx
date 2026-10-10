"use client";

import { useEffect, useState } from "react";
import type { ApiResult } from "@/types/api";
import {
  listOrderFulfilmentQueue,
  markOrderDispatched,
  markOrderProcessing,
  markOrderReady,
  updateReceiveDate,
} from "../api";
import type { Order, OrderStatus } from "../types";
import NotificationList from "./NotificationList";

const ORDER_TYPE_LABELS: Record<string, string> = {
  PROGRESSIVE: "Progressive lenses",
  SINGLE_VISION: "Single vision",
  BIFOCAL: "Bifocal",
};

const STATUS_LABELS: Record<OrderStatus, string> = {
  PENDING: "Awaiting review",
  APPROVED: "Approved",
  PROCESSING: "In the lab",
  READY: "Ready to collect",
  DISPATCHED: "Dispatched",
  REJECTED: "Rejected",
};

/**
 * The shop's production line: the orders already approved, oldest first, with the
 * one move that takes each one closer to the customer. Every move emails the
 * customer, so the buttons are labelled with the move, not the status.
 *
 * <p>Only forward moves are offered — the backend refuses a reverse — and a
 * dispatched order leaves the list because it is done. The shop can also correct
 * the estimated receive date here; that correction does not email the customer.
 *
 * <p>A client component because the whole thing is state and handlers.
 */
export default function OrderFulfilmentQueue() {
  const [items, setItems] = useState<Order[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [editingDateId, setEditingDateId] = useState<number | null>(null);
  const [dateDraft, setDateDraft] = useState("");
  const [openHistoryId, setOpenHistoryId] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    listOrderFulfilmentQueue().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "FORBIDDEN" ||
            result.error.code === "UNAUTHENTICATED"
            ? "Sign in as the shop owner to manage production."
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

  async function onAdvance(
    id: number,
    move: (id: number) => Promise<ApiResult<Order>>,
  ) {
    setError(null);
    setBusyId(id);
    const result = await move(id);
    setBusyId(null);
    if (!result.ok) {
      setError(result.error.message);
      return;
    }
    setItems((current) => {
      if (!current) {
        return current;
      }
      if (result.data.status === "DISPATCHED") {
        return current.filter((item) => item.id !== id);
      }
      return current.map((item) => (item.id === id ? result.data : item));
    });
  }

  async function onSaveDate(id: number) {
    setError(null);
    setBusyId(id);
    const result = await updateReceiveDate(id, dateDraft || null);
    setBusyId(null);
    if (!result.ok) {
      setError(result.error.message);
      return;
    }
    setEditingDateId(null);
    setItems(
      (current) =>
        current?.map((item) => (item.id === id ? result.data : item)) ?? null,
    );
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
      <p className="text-sm opacity-70">Nothing is in production right now.</p>
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
        {items.map((order) => (
          <li
            key={order.id}
            className="rounded-lg border border-black/10 p-4 dark:border-white/15"
          >
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <span className="font-medium">{order.orderNumber}</span>
              <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                {ORDER_TYPE_LABELS[order.orderType] ?? order.orderType}
              </span>
              <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                {STATUS_LABELS[order.status] ?? order.status}
              </span>
            </div>

            <div className="mt-3 text-sm">
              {editingDateId === order.id ? (
                <div className="flex flex-wrap items-end gap-2">
                  <label
                    htmlFor={`receive-date-${order.id}`}
                    className="flex flex-col gap-1 text-xs opacity-70"
                  >
                    Estimated ready
                    <input
                      id={`receive-date-${order.id}`}
                      type="date"
                      value={dateDraft}
                      onChange={(event) => setDateDraft(event.target.value)}
                      className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
                    />
                  </label>
                  <button
                    type="button"
                    disabled={busyId === order.id}
                    onClick={() => void onSaveDate(order.id)}
                    className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                  >
                    Save
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingDateId(null)}
                    className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <div className="flex flex-wrap items-center gap-2">
                  <span className="opacity-80">
                    Estimated ready:{" "}
                    {order.receiveDate
                      ? formatDate(order.receiveDate)
                      : "not set"}
                  </span>
                  <button
                    type="button"
                    onClick={() => {
                      setEditingDateId(order.id);
                      setDateDraft(order.receiveDate ?? "");
                      setError(null);
                    }}
                    className="text-xs underline"
                  >
                    Change date
                  </button>
                </div>
              )}
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {order.status === "APPROVED" ? (
                <button
                  type="button"
                  disabled={busyId === order.id}
                  onClick={() => void onAdvance(order.id, markOrderProcessing)}
                  className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                >
                  Start processing
                </button>
              ) : null}
              {order.status === "PROCESSING" ? (
                <button
                  type="button"
                  disabled={busyId === order.id}
                  onClick={() => void onAdvance(order.id, markOrderReady)}
                  className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                >
                  Mark ready
                </button>
              ) : null}
              {order.status === "READY" ? (
                <button
                  type="button"
                  disabled={busyId === order.id}
                  onClick={() => void onAdvance(order.id, markOrderDispatched)}
                  className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                >
                  Mark dispatched
                </button>
              ) : null}
              <button
                type="button"
                onClick={() =>
                  setOpenHistoryId((current) =>
                    current === order.id ? null : order.id,
                  )
                }
                className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
              >
                {openHistoryId === order.id ? "Hide updates" : "Updates"}
              </button>
            </div>

            {openHistoryId === order.id ? (
              <div className="mt-4 border-t border-black/10 pt-3 dark:border-white/15">
                <NotificationList orderId={order.id} shop />
              </div>
            ) : null}
          </li>
        ))}
      </ul>
    </div>
  );
}

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
