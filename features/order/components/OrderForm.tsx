"use client";

import Link from "next/link";
import { useEffect, useState, type FormEvent } from "react";
import { createOrder, listOwnPrescriptions } from "../api";
import type { Order, OrderType, Prescription } from "../types";

const ORDER_TYPES: { value: OrderType; label: string; hint: string }[] = [
  {
    value: "PROGRESSIVE",
    label: "Progressive lenses",
    hint: "Routed to the shop's progressive workflow.",
  },
  {
    value: "SINGLE_VISION",
    label: "Single vision",
    hint: "One prescription per lens.",
  },
  { value: "BIFOCAL", label: "Bifocal", hint: "Two viewing zones per lens." },
];

/**
 * The order form: pick one of your prescriptions, pick the order type, and link
 * the frame and lens the order is built with.
 *
 * <p>The frame and lens ids arrive from the catalog pages that will link here
 * (`/lenses`, `/frames`); those pages do not exist yet, so the values are shown
 * as editable fields pre-filled from the query string. That keeps the form usable
 * today and means it needs no change when the catalog starts linking to it.
 *
 * <p>A client component because the whole thing is state and an event handler.
 * The backend is the authority on ownership and on whether the frame and lens
 * exist; this only reports what it says.
 */
export default function OrderForm({
  initialFrameId,
  initialLensId,
}: {
  initialFrameId?: string;
  initialLensId?: string;
}) {
  const [prescriptions, setPrescriptions] = useState<Prescription[] | null>(
    null,
  );
  const [loadError, setLoadError] = useState<string | null>(null);
  const [prescriptionId, setPrescriptionId] = useState("");
  const [orderType, setOrderType] = useState<OrderType>("PROGRESSIVE");
  const [frameId, setFrameId] = useState(initialFrameId ?? "");
  const [lensId, setLensId] = useState(initialLensId ?? "");
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState<Order | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let active = true;
    listOwnPrescriptions().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "UNAUTHENTICATED"
            ? "Please sign in to place an order."
            : result.error.message,
        );
        return;
      }
      setPrescriptions(result.data);
      const firstOrderable = result.data.find(
        (prescription) => prescription.verificationStatus !== "REJECTED",
      );
      if (firstOrderable) {
        setPrescriptionId(String(firstOrderable.id));
      }
    });
    return () => {
      active = false;
    };
  }, []);

  async function onSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    if (!prescriptionId) {
      setError("Choose a prescription.");
      return;
    }
    if (!lensId) {
      setError("Enter the lens you are ordering.");
      return;
    }

    setSubmitting(true);
    const result = await createOrder({
      prescriptionId: Number(prescriptionId),
      orderType,
      lensId: Number(lensId),
      frameId: frameId ? Number(frameId) : null,
    });
    setSubmitting(false);

    if (!result.ok) {
      setError(
        result.error.code === "UNAUTHENTICATED"
          ? "Please sign in to place an order."
          : result.error.message,
      );
      return;
    }
    setSubmitted(result.data);
  }

  if (submitted) {
    return (
      <div className="max-w-prose rounded-lg border border-black/10 p-6 dark:border-white/15">
        <h2 className="text-lg font-medium">Order placed</h2>
        <p className="mt-2 text-sm opacity-80">
          Reference {submitted.orderNumber}. Your prescription, frame and lens
          are linked and will be processed together.
        </p>
        <p className="mt-1 text-sm opacity-80">
          Status: {submitted.status.replace(/_/g, " ").toLowerCase()}.
        </p>
        <Link
          href="/orders/new"
          className="mt-4 inline-block text-sm underline"
        >
          Place another order
        </Link>
      </div>
    );
  }

  return (
    <form onSubmit={onSubmit} className="flex max-w-2xl flex-col gap-6">
      <div className="flex flex-col gap-2">
        <label htmlFor="prescription" className="text-sm font-medium">
          Prescription
        </label>
        {loadError ? (
          <p role="alert" className="text-sm text-red-600 dark:text-red-400">
            {loadError}
          </p>
        ) : (
          <select
            id="prescription"
            value={prescriptionId}
            onChange={(event) => setPrescriptionId(event.target.value)}
            disabled={prescriptions === null}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          >
            {prescriptions === null ? <option>Loading…</option> : null}
            {prescriptions?.length === 0 ? (
              <option value="">No prescriptions yet</option>
            ) : null}
            {prescriptions?.map((prescription) => (
              <option
                key={prescription.id}
                value={prescription.id}
                disabled={prescription.verificationStatus === "REJECTED"}
              >
                #{prescription.id}
                {prescription.progressive ? " (progressive)" : ""} —{" "}
                {prescription.verificationStatus
                  .replace(/_/g, " ")
                  .toLowerCase()}
              </option>
            ))}
          </select>
        )}
        <p className="text-xs opacity-70">
          Don&apos;t have one?{" "}
          <Link href="/prescriptions" className="underline">
            Submit a prescription
          </Link>
          .
        </p>
      </div>

      <div className="flex flex-col gap-2">
        <label htmlFor="order-type" className="text-sm font-medium">
          Order type
        </label>
        <select
          id="order-type"
          value={orderType}
          onChange={(event) => setOrderType(event.target.value as OrderType)}
          className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
        >
          {ORDER_TYPES.map((type) => (
            <option key={type.value} value={type.value}>
              {type.label}
            </option>
          ))}
        </select>
        <p className="text-xs opacity-70">
          {ORDER_TYPES.find((type) => type.value === orderType)?.hint}
        </p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <div className="flex flex-col gap-1">
          <label htmlFor="frame-id" className="text-sm font-medium">
            Frame (optional)
          </label>
          <input
            id="frame-id"
            inputMode="numeric"
            placeholder="From the Frames page"
            value={frameId}
            onChange={(event) => setFrameId(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </div>
        <div className="flex flex-col gap-1">
          <label htmlFor="lens-id" className="text-sm font-medium">
            Lens
          </label>
          <input
            id="lens-id"
            inputMode="numeric"
            placeholder="From the Lenses page"
            value={lensId}
            onChange={(event) => setLensId(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </div>
      </div>

      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      <button
        type="submit"
        disabled={submitting}
        className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background disabled:opacity-50"
      >
        {submitting ? "Placing…" : "Place order"}
      </button>
    </form>
  );
}
