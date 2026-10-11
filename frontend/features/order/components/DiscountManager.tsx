"use client";

import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import { listShopFrames, listShopLenses } from "@/features/catalog/api";
import type { Frame, Lens } from "@/features/catalog/types";
import {
  createDiscount,
  endDiscount,
  listShopDiscounts,
  updateDiscount,
} from "../api";
import type { Discount, DiscountInput, DiscountState } from "../types";

const PERCENT_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 0,
  maximumFractionDigits: 2,
});

const STATE_LABEL: Record<DiscountState, string> = {
  ACTIVE: "Running",
  SCHEDULED: "Scheduled",
  EXPIRED: "Expired",
  ENDED: "Ended",
};

/**
 * The shop owner's promotions: create a discount so a promotion can run, edit one
 * to adjust it, and end one early when the offer stops.
 *
 * <p>A client component because the whole thing is state and handlers. The
 * backend is the authority on validation and on the role boundary; this only
 * reports what it says. Target items are picked from the live catalogue, which
 * this reads through the catalog feature's own API — a deliberate read of another
 * feature's endpoints for a dropdown, not a shared write.
 */
export default function DiscountManager() {
  const [discounts, setDiscounts] = useState<Discount[] | null>(null);
  const [frames, setFrames] = useState<Frame[]>([]);
  const [lenses, setLenses] = useState<Lens[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    listShopDiscounts().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(messageFor(result.error));
        return;
      }
      setLoadError(null);
      setDiscounts(result.data);
    });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    Promise.all([listShopFrames(), listShopLenses()]).then(
      ([framesResult, lensesResult]) => {
        if (!active) {
          return;
        }
        if (framesResult.ok) {
          setFrames(framesResult.data);
        }
        if (lensesResult.ok) {
          setLenses(lensesResult.data);
        }
      },
    );
    return () => {
      active = false;
    };
  }, []);

  async function onCreate(input: DiscountInput): Promise<string | null> {
    const result = await createDiscount(input);
    if (!result.ok) {
      return messageFor(result.error);
    }
    setDiscounts((current) => [...(current ?? []), result.data]);
    setAdding(false);
    return null;
  }

  async function onUpdate(
    id: number,
    input: DiscountInput,
  ): Promise<string | null> {
    const result = await updateDiscount(id, input);
    if (!result.ok) {
      return messageFor(result.error);
    }
    setDiscounts(
      (current) =>
        current?.map((discount) =>
          discount.id === id ? result.data : discount,
        ) ?? null,
    );
    setEditingId(null);
    return null;
  }

  async function onEnd(id: number): Promise<void> {
    const result = await endDiscount(id);
    if (!result.ok) {
      setActionError(messageFor(result.error));
      return;
    }
    setActionError(null);
    setDiscounts(
      (current) =>
        current?.map((discount) =>
          discount.id === id ? result.data : discount,
        ) ?? null,
    );
  }

  if (loadError) {
    return (
      <p role="alert" className="text-sm text-red-600 dark:text-red-400">
        {loadError}
      </p>
    );
  }

  if (discounts === null) {
    return <p className="text-sm opacity-70">Loading…</p>;
  }

  return (
    <div className="flex flex-col gap-4">
      {actionError ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {actionError}
        </p>
      ) : null}

      {editingId === null && !adding ? (
        <button
          type="button"
          onClick={() => setAdding(true)}
          className="w-fit rounded-md bg-foreground px-4 py-2 text-sm font-medium text-background"
        >
          Add discount
        </button>
      ) : null}

      {adding ? (
        <div className="rounded-lg border border-black/10 p-4 dark:border-white/15">
          <h2 className="text-base font-medium">New discount</h2>
          <DiscountForm
            frames={frames}
            lenses={lenses}
            submitLabel="Add discount"
            onSubmit={onCreate}
            onCancel={() => setAdding(false)}
          />
        </div>
      ) : null}

      {discounts.length === 0 && !adding ? (
        <p className="text-sm opacity-70">
          No promotions yet. Add the first one to put it on the shop.
        </p>
      ) : null}

      <ul className="flex flex-col gap-4">
        {discounts.map((discount) =>
          editingId === discount.id ? (
            <li
              key={discount.id}
              className="rounded-lg border border-black/10 p-4 dark:border-white/15"
            >
              <h2 className="text-base font-medium">Edit discount</h2>
              <DiscountForm
                initial={discount}
                frames={frames}
                lenses={lenses}
                submitLabel="Save changes"
                onSubmit={(input) => onUpdate(discount.id, input)}
                onCancel={() => setEditingId(null)}
              />
            </li>
          ) : (
            <li
              key={discount.id}
              className="rounded-lg border border-black/10 p-4 dark:border-white/15"
            >
              <div className="flex flex-wrap items-center gap-2 text-sm">
                <span className="font-medium">{discount.description}</span>
                <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                  {STATE_LABEL[discount.state]}
                </span>
              </div>
              <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-sm sm:grid-cols-4">
                <Detail
                  label="Discount"
                  value={`${PERCENT_FORMAT.format(discount.percentage)}%`}
                />
                <Detail label="From" value={discount.validFrom} />
                <Detail label="To" value={discount.validTo} />
                <Detail label="Applies to" value={targetSummary(discount)} />
              </dl>
              <div className="mt-4 flex gap-2">
                {discount.active ? (
                  <>
                    <button
                      type="button"
                      onClick={() => {
                        setAdding(false);
                        setEditingId(discount.id);
                      }}
                      className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                    >
                      Edit
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        if (
                          window.confirm(
                            `End "${discount.description}" now? It stops running but stays in the history.`,
                          )
                        ) {
                          void onEnd(discount.id);
                        }
                      }}
                      className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                    >
                      End early
                    </button>
                  </>
                ) : (
                  <span className="text-xs opacity-70">
                    Ended {discount.endedAt?.slice(0, 10)}
                  </span>
                )}
              </div>
            </li>
          ),
        )}
      </ul>
    </div>
  );
}

/**
 * The add and edit form. It validates the fields the backend marks required so a
 * wrong value is caught before a round trip, then reports whatever the backend
 * refuses in the same place. It hands the parsed input to its parent, which owns
 * the network call and the list update.
 */
function DiscountForm({
  initial,
  frames,
  lenses,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial?: Discount;
  frames: Frame[];
  lenses: Lens[];
  submitLabel: string;
  onSubmit: (input: DiscountInput) => Promise<string | null>;
  onCancel: () => void;
}) {
  const [description, setDescription] = useState(initial?.description ?? "");
  const [percentage, setPercentage] = useState(
    initial ? String(initial.percentage) : "",
  );
  const [validFrom, setValidFrom] = useState(initial?.validFrom ?? "");
  const [validTo, setValidTo] = useState(initial?.validTo ?? "");
  const [sitewide, setSitewide] = useState(initial?.sitewide ?? true);
  const [frameIds, setFrameIds] = useState<number[]>(
    initial?.targetItemIds ?? [],
  );
  const [lensIds, setLensIds] = useState<number[]>(
    initial?.targetItemIds ?? [],
  );
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  function toggle(
    list: number[],
    id: number,
    setter: (next: number[]) => void,
  ) {
    setter(
      list.includes(id) ? list.filter((value) => value !== id) : [...list, id],
    );
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedDescription = description.trim();
    if (!trimmedDescription) {
      setError("Describe the promotion.");
      return;
    }
    const percentageNumber = Number(percentage);
    if (
      percentage.trim() === "" ||
      Number.isNaN(percentageNumber) ||
      percentageNumber <= 0 ||
      percentageNumber > 100
    ) {
      setError("Enter a percentage greater than 0 and no more than 100.");
      return;
    }
    if (!validFrom || !validTo) {
      setError("Choose both the start and end dates.");
      return;
    }
    if (validTo < validFrom) {
      setError("The end date cannot be before the start date.");
      return;
    }

    const targetItemIds = sitewide
      ? null
      : selectedTargets(frames, lenses, frameIds, lensIds);
    if (!sitewide && (targetItemIds ?? []).length === 0) {
      setError(
        "Pick at least one frame or lens, or make the discount sitewide.",
      );
      return;
    }

    setError(null);
    setBusy(true);
    const message = await onSubmit({
      description: trimmedDescription,
      percentage: percentageNumber,
      targetItemIds,
      validFrom,
      validTo,
    });
    setBusy(false);
    if (message) {
      setError(message);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-3 flex flex-col gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Description">
          <input
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Percentage off">
          <input
            type="number"
            min="0.01"
            max="100"
            step="0.01"
            inputMode="decimal"
            value={percentage}
            onChange={(event) => setPercentage(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Starts">
          <input
            type="date"
            value={validFrom}
            onChange={(event) => setValidFrom(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Ends">
          <input
            type="date"
            value={validTo}
            onChange={(event) => setValidTo(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={sitewide}
          onChange={(event) => setSitewide(event.target.checked)}
        />
        Apply to everything (sitewide)
      </label>

      {!sitewide ? (
        <div className="grid gap-4 sm:grid-cols-2">
          <Fieldset label="Frames">
            {frames.length === 0 ? (
              <p className="text-xs opacity-70">No frames in the catalogue.</p>
            ) : (
              frames.map((frame) => (
                <Checkbox
                  key={`frame-${frame.id}`}
                  checked={frameIds.includes(frame.id)}
                  label={`${frame.model} (#${frame.id})`}
                  onChange={() => toggle(frameIds, frame.id, setFrameIds)}
                />
              ))
            )}
          </Fieldset>
          <Fieldset label="Lenses">
            {lenses.length === 0 ? (
              <p className="text-xs opacity-70">No lenses in the collection.</p>
            ) : (
              lenses.map((lens) => (
                <Checkbox
                  key={`lens-${lens.id}`}
                  checked={lensIds.includes(lens.id)}
                  label={`${lens.name} (#${lens.id})`}
                  onChange={() => toggle(lensIds, lens.id, setLensIds)}
                />
              ))
            )}
          </Fieldset>
        </div>
      ) : null}

      {error ? (
        <p role="alert" className="text-sm text-red-600 dark:text-red-400">
          {error}
        </p>
      ) : null}

      <div className="flex gap-2">
        <button
          type="submit"
          disabled={busy}
          className="rounded-md bg-foreground px-3 py-1.5 text-sm font-medium text-background disabled:opacity-50"
        >
          {busy ? "Saving…" : submitLabel}
        </button>
        <button
          type="button"
          disabled={busy}
          onClick={onCancel}
          className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
        >
          Cancel
        </button>
      </div>
    </form>
  );
}

/**
 * The frame and lens ids chosen for the promotion. Both features' ids share the
 * one JSON array on the backend, which is the documented shape of
 * {@code target_item_ids}; ids are listed frames first, then lenses.
 */
function selectedTargets(
  frames: Frame[],
  lenses: Lens[],
  frameIds: number[],
  lensIds: number[],
): number[] {
  return [
    ...frames
      .filter((frame) => frameIds.includes(frame.id))
      .map((frame) => frame.id),
    ...lenses
      .filter((lens) => lensIds.includes(lens.id))
      .map((lens) => lens.id),
  ];
}

function targetSummary(discount: Discount): string {
  if (discount.sitewide) {
    return "Everything";
  }
  return `${discount.targetItemIds?.length ?? 0} item(s)`;
}

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium">
      {label}
      {children}
    </label>
  );
}

function Fieldset({ label, children }: { label: string; children: ReactNode }) {
  return (
    <fieldset className="flex flex-col gap-1 rounded-md border border-black/10 p-3 dark:border-white/15">
      <legend className="px-1 text-sm font-medium">{label}</legend>
      {children}
    </fieldset>
  );
}

function Checkbox({
  checked,
  label,
  onChange,
}: {
  checked: boolean;
  label: string;
  onChange: () => void;
}) {
  return (
    <label className="flex items-center gap-2 text-sm">
      <input type="checkbox" checked={checked} onChange={onChange} />
      {label}
    </label>
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

function messageFor(error: { code: string; message: string }): string {
  return error.code === "FORBIDDEN" || error.code === "UNAUTHENTICATED"
    ? "Sign in as the shop owner to manage discounts."
    : error.message;
}
