"use client";

import { useEffect, useState } from "react";
import { approveOrder, listOrderApprovalQueue, rejectOrder } from "../api";
import type { Order } from "../types";

const ORDER_TYPE_LABELS: Record<string, string> = {
  PROGRESSIVE: "Progressive lenses",
  SINGLE_VISION: "Single vision",
  BIFOCAL: "Bifocal",
};

/**
 * The shop owner's order approval queue: each pending order with the prescription,
 * frame and lens it is built from, so the owner can approve it into production or
 * reject it with a reason.
 *
 * <p>Approval is refused by the backend while the linked prescription has not been
 * verified; that refusal is reported as "verify the prescription first" rather than
 * a bare error. A client component because the whole thing is state and handlers.
 */
export default function OrderApprovalQueue() {
  const [items, setItems] = useState<Order[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [rejectingId, setRejectingId] = useState<number | null>(null);
  const [reason, setReason] = useState("");

  useEffect(() => {
    let active = true;
    listOrderApprovalQueue("PENDING").then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "FORBIDDEN" ||
            result.error.code === "UNAUTHENTICATED"
            ? "Sign in as the shop owner to approve orders."
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

  async function onApprove(id: number) {
    setError(null);
    setBusyId(id);
    const result = await approveOrder(id);
    setBusyId(null);
    if (!result.ok) {
      setError(
        result.error.code === "PRESCRIPTION_NOT_VERIFIED"
          ? "That order's prescription has not been verified yet. Verify it first."
          : result.error.message,
      );
      return;
    }
    setItems((current) => current?.filter((item) => item.id !== id) ?? null);
  }

  async function onReject(id: number) {
    const trimmed = reason.trim();
    if (!trimmed) {
      setError("Say why the order is being rejected.");
      return;
    }
    setError(null);
    setBusyId(id);
    const result = await rejectOrder(id, trimmed);
    setBusyId(null);
    if (!result.ok) {
      setError(result.error.message);
      return;
    }
    setRejectingId(null);
    setReason("");
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
        Nothing is waiting for approval right now.
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
            </div>
            <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-sm sm:grid-cols-4">
              <Detail label="Prescription" value={`#${order.prescriptionId}`} />
              <Detail
                label="Frame"
                value={order.frameId ? `#${order.frameId}` : "None"}
              />
              <Detail label="Lens" value={`#${order.lensId}`} />
              <Detail label="Quantity" value={String(order.quantity)} />
            </dl>

            {rejectingId === order.id ? (
              <div className="mt-4 flex flex-col gap-2">
                <label
                  htmlFor={`order-reason-${order.id}`}
                  className="text-sm font-medium"
                >
                  Why is this order being rejected?
                </label>
                <input
                  id={`order-reason-${order.id}`}
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="e.g. Lens out of stock."
                  className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
                />
                <div className="flex gap-2">
                  <button
                    type="button"
                    disabled={busyId === order.id}
                    onClick={() => void onReject(order.id)}
                    className="rounded-md bg-red-600 px-3 py-1.5 text-sm font-medium text-white disabled:opacity-50"
                  >
                    Confirm rejection
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setRejectingId(null);
                      setReason("");
                    }}
                    className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            ) : (
              <div className="mt-4 flex gap-2">
                <button
                  type="button"
                  disabled={busyId === order.id}
                  onClick={() => void onApprove(order.id)}
                  className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                >
                  Approve
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setRejectingId(order.id);
                    setReason("");
                    setError(null);
                  }}
                  className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                >
                  Reject…
                </button>
              </div>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex justify-between gap-2 sm:flex-col sm:gap-0">
      <dt className="text-xs opacity-70">{label}</dt>
      <dd>{value}</dd>
    </div>
  );
}
