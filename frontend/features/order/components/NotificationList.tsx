"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { listOrderNotifications, listOwnNotifications } from "../api";
import type { OrderNotification, NotificationStatus } from "../types";

const STATUS_LABELS: Record<NotificationStatus, string> = {
  PENDING: "Sending",
  SENT: "Sent",
  FAILED: "Not sent",
};

/**
 * The messages the shop has sent about an order.
 *
 * <p>Two readers, two endpoints: the customer reads their own through
 * `/api/notifications`, while the shop reads one order's history through
 * `/api/shop/orders/{id}/notifications`. The `shop` flag picks the endpoint so
 * the row rendering stays in one place. A client component because it loads.
 */
export default function NotificationList({
  orderId,
  shop = false,
  emptyText = "No notifications yet.",
}: {
  orderId?: number;
  shop?: boolean;
  emptyText?: string;
}) {
  const [items, setItems] = useState<OrderNotification[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    const load = shop
      ? listOrderNotifications(orderId as number)
      : listOwnNotifications(orderId);
    load.then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "UNAUTHENTICATED" ||
            result.error.code === "FORBIDDEN"
            ? "Please sign in to see notifications."
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
  }, [orderId, shop]);

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
    return <p className="text-sm opacity-70">{emptyText}</p>;
  }

  return (
    <ul className="flex flex-col gap-3">
      {items.map((notification) => (
        <li
          key={notification.id}
          className="rounded-lg border border-black/10 p-3 text-sm dark:border-white/15"
        >
          <div className="flex flex-wrap items-center gap-2">
            {shop ? (
              <span className="font-medium">{notification.orderNumber}</span>
            ) : (
              <Link
                href={`/orders/${notification.orderId}`}
                className="font-medium underline"
              >
                {notification.orderNumber}
              </Link>
            )}
            <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
              {STATUS_LABELS[notification.status] ?? notification.status}
            </span>
            {notification.sentAt ? (
              <span className="text-xs opacity-70">
                {new Date(notification.sentAt).toLocaleString()}
              </span>
            ) : null}
          </div>
          <p className="mt-2 opacity-80">{notification.message}</p>
        </li>
      ))}
    </ul>
  );
}
