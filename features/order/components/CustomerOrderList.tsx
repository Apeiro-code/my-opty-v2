"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { listOwnOrders } from "../api";
import type { Order, OrderStatus } from "../types";

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
 * The customer's own orders, newest first, each linking to its detail page. The
 * receive date is shown here too, because "when do I get my glasses" is the
 * question the list is opened to answer.
 *
 * <p>A client component because the whole thing is a load and a render. The
 * backend scopes the list to the session's customer; this never passes an id.
 */
export default function CustomerOrderList() {
  const [items, setItems] = useState<Order[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listOwnOrders().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "UNAUTHENTICATED"
            ? "Please sign in to see your orders."
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
      <p className="text-sm opacity-70">You have not placed any orders yet.</p>
    );
  }

  return (
    <ul className="flex flex-col gap-4">
      {items.map((order) => (
        <li
          key={order.id}
          className="rounded-lg border border-black/10 p-4 dark:border-white/15"
        >
          <div className="flex flex-wrap items-center gap-2 text-sm">
            <Link
              href={`/orders/${order.id}`}
              className="font-medium underline"
            >
              {order.orderNumber}
            </Link>
            <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
              {ORDER_TYPE_LABELS[order.orderType] ?? order.orderType}
            </span>
            <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
              {STATUS_LABELS[order.status] ?? order.status}
            </span>
          </div>
          <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-sm sm:grid-cols-4">
            <div className="flex justify-between gap-2 sm:flex-col sm:gap-0">
              <dt className="text-xs opacity-70">Placed</dt>
              <dd>{formatDate(order.orderDate)}</dd>
            </div>
            <div className="flex justify-between gap-2 sm:flex-col sm:gap-0">
              <dt className="text-xs opacity-70">Estimated ready</dt>
              <dd>{order.receiveDate ? formatDate(order.receiveDate) : "—"}</dd>
            </div>
            <div className="flex justify-between gap-2 sm:flex-col sm:gap-0">
              <dt className="text-xs opacity-70">Quantity</dt>
              <dd>{order.quantity}</dd>
            </div>
          </dl>
        </li>
      ))}
    </ul>
  );
}

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
