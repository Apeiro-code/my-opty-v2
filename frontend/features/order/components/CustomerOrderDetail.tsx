"use client";

import { useEffect, useState } from "react";
import { readOrder } from "../api";
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
 * One order as its owner sees it: the linked prescription, frame and lens, the
 * estimated receive date, and the notifications the shop has sent about it.
 *
 * <p>A client component because it loads the order on mount. The backend scopes
 * the read to the session's customer, so a wrong id is a 404, not a leak.
 */
export default function CustomerOrderDetail({ orderId }: { orderId: number }) {
  const [order, setOrder] = useState<Order | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    readOrder(orderId).then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "NOT_FOUND"
            ? "That order does not exist."
            : result.error.message,
        );
        return;
      }
      setLoadError(null);
      setOrder(result.data);
    });
    return () => {
      active = false;
    };
  }, [orderId]);

  if (loadError) {
    return (
      <p role="alert" className="text-sm text-red-600 dark:text-red-400">
        {loadError}
      </p>
    );
  }

  if (order === null) {
    return <p className="text-sm opacity-70">Loading…</p>;
  }

  return (
    <div className="flex flex-col gap-6">
      <div className="rounded-lg border border-black/10 p-4 dark:border-white/15">
        <div className="flex flex-wrap items-center gap-2 text-sm">
          <span className="font-medium">{order.orderNumber}</span>
          <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
            {ORDER_TYPE_LABELS[order.orderType] ?? order.orderType}
          </span>
          <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
            {STATUS_LABELS[order.status] ?? order.status}
          </span>
        </div>

        <dl className="mt-4 grid grid-cols-2 gap-x-4 gap-y-2 text-sm sm:grid-cols-4">
          <Detail label="Prescription" value={`#${order.prescriptionId}`} />
          <Detail
            label="Frame"
            value={order.frameId ? `#${order.frameId}` : "None"}
          />
          <Detail label="Lens" value={`#${order.lensId}`} />
          <Detail label="Quantity" value={String(order.quantity)} />
          <Detail label="Placed" value={formatDate(order.orderDate)} />
          <Detail
            label="Estimated ready"
            value={order.receiveDate ? formatDate(order.receiveDate) : "—"}
          />
        </dl>

        {order.status === "REJECTED" && order.rejectionReason ? (
          <p className="mt-4 text-sm text-red-600 dark:text-red-400">
            Rejected: {order.rejectionReason}
          </p>
        ) : null}
      </div>

      <div className="flex flex-col gap-3">
        <h2 className="text-lg font-medium">Updates</h2>
        <NotificationList
          orderId={orderId}
          emptyText="No updates about this order yet."
        />
      </div>
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

function formatDate(value: string): string {
  return new Date(value).toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
}
