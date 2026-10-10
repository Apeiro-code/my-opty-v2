"use client";

import { useEffect, useState } from "react";
import {
  listPrescriptionReviewQueue,
  rejectPrescription,
  verifyPrescription,
} from "../api";
import type { Prescription } from "../types";

/**
 * The shop owner's prescription review queue: each pending prescription with its
 * optical values, so the owner can check it for completeness and either verify it
 * or reject it with a reason.
 *
 * <p>A client component because the whole thing is state and event handlers: it
 * loads the queue, then updates it in place as decisions are made. The backend is
 * the authority; this only reports what it says.
 */
export default function PrescriptionReviewQueue() {
  const [items, setItems] = useState<Prescription[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busyId, setBusyId] = useState<number | null>(null);
  const [rejectingId, setRejectingId] = useState<number | null>(null);
  const [reason, setReason] = useState("");

  useEffect(() => {
    let active = true;
    listPrescriptionReviewQueue("PENDING_REVIEW").then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(
          result.error.code === "FORBIDDEN" ||
            result.error.code === "UNAUTHENTICATED"
            ? "Sign in as the shop owner to review prescriptions."
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

  async function onVerify(id: number) {
    setError(null);
    setBusyId(id);
    const result = await verifyPrescription(id);
    setBusyId(null);
    if (!result.ok) {
      setError(result.error.message);
      return;
    }
    setItems((current) => current?.filter((item) => item.id !== id) ?? null);
  }

  async function onReject(id: number) {
    const trimmed = reason.trim();
    if (!trimmed) {
      setError("Say why the prescription is being rejected.");
      return;
    }
    setError(null);
    setBusyId(id);
    const result = await rejectPrescription(id, trimmed);
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
        Nothing is waiting for review right now.
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
        {items.map((prescription) => (
          <li
            key={prescription.id}
            className="rounded-lg border border-black/10 p-4 dark:border-white/15"
          >
            <div className="flex flex-wrap items-center gap-2 text-sm">
              <span className="font-medium">#{prescription.id}</span>
              {prescription.progressive ? (
                <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                  Progressive
                </span>
              ) : null}
              <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                {prescription.hasDocument ? "Document" : "No document"}
              </span>
            </div>

            <div className="mt-3 grid gap-3 sm:grid-cols-2">
              <EyeReading
                legend="Left eye (OS)"
                sph={prescription.sphLeft}
                cyl={prescription.cylLeft}
                axis={prescription.axisLeft}
                add={prescription.addPowerLeft}
              />
              <EyeReading
                legend="Right eye (OD)"
                sph={prescription.sphRight}
                cyl={prescription.cylRight}
                axis={prescription.axisRight}
                add={prescription.addPowerRight}
              />
            </div>

            {rejectingId === prescription.id ? (
              <div className="mt-4 flex flex-col gap-2">
                <label
                  htmlFor={`reason-${prescription.id}`}
                  className="text-sm font-medium"
                >
                  What is missing or wrong?
                </label>
                <input
                  id={`reason-${prescription.id}`}
                  value={reason}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="e.g. Add power is missing for the left eye."
                  className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
                />
                <div className="flex gap-2">
                  <button
                    type="button"
                    disabled={busyId === prescription.id}
                    onClick={() => void onReject(prescription.id)}
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
                  disabled={busyId === prescription.id}
                  onClick={() => void onVerify(prescription.id)}
                  className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
                >
                  Verify
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setRejectingId(prescription.id);
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

function EyeReading({
  legend,
  sph,
  cyl,
  axis,
  add,
}: {
  legend: string;
  sph: string | null;
  cyl: string | null;
  axis: string | null;
  add: string | null;
}) {
  const values = [
    ["SPH", sph],
    ["CYL", cyl],
    ["AXIS", axis],
    ["ADD", add],
  ] as const;

  return (
    <fieldset className="rounded-md border border-black/10 p-3 dark:border-white/15">
      <legend className="px-1 text-xs font-medium">{legend}</legend>
      <dl className="grid grid-cols-2 gap-x-4 gap-y-1 text-sm">
        {values.map(([label, value]) => (
          <div key={label} className="flex justify-between gap-2">
            <dt className="text-xs opacity-70">{label}</dt>
            <dd className={value ? "" : "opacity-40"}>{value ?? "—"}</dd>
          </div>
        ))}
      </dl>
    </fieldset>
  );
}
