"use client";

import { useEffect, useState, type FormEvent, type ReactNode } from "react";
import {
  createFrame,
  discontinueFrame,
  listShopCategories,
  listShopFrames,
  updateFrame,
} from "../api";
import type { Category, Frame, FrameInput } from "../types";
import CategoryPicker from "./CategoryPicker";

const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/**
 * The shop owner's frame catalogue: add a frame so it appears on the site, and
 * edit one so its details stay accurate.
 *
 * <p>A client component because the whole thing is state and handlers. The
 * backend is the authority on validation and on the role boundary; this only
 * reports what it says, turning a 403/401 into "sign in as the shop owner".
 */
export default function FrameManager() {
  const [frames, setFrames] = useState<Frame[] | null>(null);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [adding, setAdding] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  useEffect(() => {
    let active = true;
    listShopFrames().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(messageFor(result.error));
        return;
      }
      setLoadError(null);
      setFrames(result.data);
    });
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    listShopCategories().then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(messageFor(result.error));
        return;
      }
      setCategories(result.data);
    });
    return () => {
      active = false;
    };
  }, []);

  async function onCreate(input: FrameInput): Promise<string | null> {
    const result = await createFrame(input);
    if (!result.ok) {
      return messageFor(result.error);
    }
    setFrames((current) => [...(current ?? []), result.data]);
    setAdding(false);
    return null;
  }

  async function onUpdate(
    id: number,
    input: FrameInput,
  ): Promise<string | null> {
    const result = await updateFrame(id, input);
    if (!result.ok) {
      return messageFor(result.error);
    }
    setFrames(
      (current) =>
        current?.map((frame) => (frame.id === id ? result.data : frame)) ??
        null,
    );
    setEditingId(null);
    return null;
  }

  async function onDiscontinue(id: number): Promise<void> {
    const result = await discontinueFrame(id);
    if (!result.ok) {
      setActionError(messageFor(result.error));
      return;
    }
    setActionError(null);
    setFrames(
      (current) =>
        current?.map((frame) => (frame.id === id ? result.data : frame)) ??
        null,
    );
  }

  if (loadError) {
    return (
      <p role="alert" className="text-sm text-red-600 dark:text-red-400">
        {loadError}
      </p>
    );
  }

  if (frames === null) {
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
          Add frame
        </button>
      ) : null}

      {adding ? (
        <div className="rounded-lg border border-black/10 p-4 dark:border-white/15">
          <h2 className="text-base font-medium">New frame</h2>
          <FrameForm
            categories={categories}
            submitLabel="Add frame"
            onSubmit={onCreate}
            onCancel={() => setAdding(false)}
          />
        </div>
      ) : null}

      {frames.length === 0 && !adding ? (
        <p className="text-sm opacity-70">
          No frames yet. Add the first one to put it on the site.
        </p>
      ) : null}

      <ul className="flex flex-col gap-4">
        {frames.map((frame) =>
          editingId === frame.id ? (
            <li
              key={frame.id}
              className="rounded-lg border border-black/10 p-4 dark:border-white/15"
            >
              <h2 className="text-base font-medium">Edit frame</h2>
              <FrameForm
                initial={frame}
                categories={categories}
                submitLabel="Save changes"
                onSubmit={(input) => onUpdate(frame.id, input)}
                onCancel={() => setEditingId(null)}
              />
            </li>
          ) : (
            <li
              key={frame.id}
              className="rounded-lg border border-black/10 p-4 dark:border-white/15"
            >
              <div className="flex flex-wrap items-center gap-2 text-sm">
                <span className="font-medium">{frame.model}</span>
                {frame.color ? (
                  <span className="opacity-70">{frame.color}</span>
                ) : null}
                {!frame.active ? (
                  <span className="rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10">
                    Hidden from the site
                  </span>
                ) : null}
              </div>
              <dl className="mt-3 grid grid-cols-2 gap-x-4 gap-y-1 text-sm sm:grid-cols-4">
                <Detail label="Material" value={frame.material} />
                <Detail
                  label="Category"
                  value={categoryName(categories, frame.categoryId)}
                />
                <Detail
                  label="Price"
                  value={PRICE_FORMAT.format(frame.price)}
                />
                <Detail label="In stock" value={String(frame.stockQty)} />
                <Detail label="Visible" value={frame.active ? "Yes" : "No"} />
              </dl>
              <div className="mt-4 flex gap-2">
                <button
                  type="button"
                  onClick={() => {
                    setAdding(false);
                    setEditingId(frame.id);
                  }}
                  className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                >
                  Edit
                </button>
                {frame.active ? (
                  <button
                    type="button"
                    onClick={() => {
                      if (
                        window.confirm(
                          `Discontinue "${frame.model}"? It comes off the site but stays for old orders.`,
                        )
                      ) {
                        void onDiscontinue(frame.id);
                      }
                    }}
                    className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
                  >
                    Discontinue
                  </button>
                ) : null}
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
 * the network call and the list update, and displays the returned error, if any.
 */
function FrameForm({
  initial,
  categories,
  submitLabel,
  onSubmit,
  onCancel,
}: {
  initial?: Frame;
  categories: Category[];
  submitLabel: string;
  onSubmit: (input: FrameInput) => Promise<string | null>;
  onCancel: () => void;
}) {
  const [model, setModel] = useState(initial?.model ?? "");
  const [color, setColor] = useState(initial?.color ?? "");
  const [material, setMaterial] = useState(initial?.material ?? "");
  const [price, setPrice] = useState(initial ? String(initial.price) : "");
  const [stockQty, setStockQty] = useState(
    initial ? String(initial.stockQty) : "",
  );
  const [categoryId, setCategoryId] = useState(
    initial?.categoryId ? String(initial.categoryId) : "",
  );
  const [active, setActive] = useState(initial?.active ?? true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const trimmedModel = model.trim();
    if (!trimmedModel) {
      setError("Enter a model name.");
      return;
    }
    const trimmedMaterial = material.trim();
    if (!trimmedMaterial) {
      setError("Enter a material.");
      return;
    }
    const priceNumber = Number(price);
    if (price.trim() === "" || Number.isNaN(priceNumber) || priceNumber < 0) {
      setError("Enter a price of zero or more.");
      return;
    }
    const stockNumber = Number(stockQty);
    if (
      stockQty.trim() === "" ||
      !Number.isInteger(stockNumber) ||
      stockNumber < 0
    ) {
      setError("Enter a whole stock quantity of zero or more.");
      return;
    }

    setError(null);
    setBusy(true);
    const message = await onSubmit({
      model: trimmedModel,
      color: color.trim() || null,
      material: trimmedMaterial,
      price: priceNumber,
      stockQty: stockNumber,
      active,
      categoryId: categoryId ? Number(categoryId) : null,
    });
    setBusy(false);
    if (message) {
      setError(message);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="mt-3 flex flex-col gap-4">
      <div className="grid gap-4 sm:grid-cols-2">
        <Field label="Model">
          <input
            value={model}
            onChange={(event) => setModel(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Colour">
          <input
            value={color}
            onChange={(event) => setColor(event.target.value)}
            placeholder="Optional"
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Material">
          <input
            value={material}
            onChange={(event) => setMaterial(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Price">
          <input
            type="number"
            min="0"
            step="0.01"
            inputMode="decimal"
            value={price}
            onChange={(event) => setPrice(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Stock quantity">
          <input
            type="number"
            min="0"
            step="1"
            inputMode="numeric"
            value={stockQty}
            onChange={(event) => setStockQty(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </Field>
        <Field label="Category">
          <CategoryPicker
            categories={categories}
            itemType="FRAME"
            value={categoryId}
            onChange={setCategoryId}
          />
        </Field>
      </div>

      <label className="flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={active}
          onChange={(event) => setActive(event.target.checked)}
        />
        Show this frame on the site
      </label>

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

function Field({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="flex flex-col gap-1 text-sm font-medium">
      {label}
      {children}
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

function categoryName(categories: Category[], id: number | null): string {
  if (id === null) {
    return "Unfiled";
  }
  return categories.find((category) => category.id === id)?.name ?? "—";
}

function messageFor(error: { code: string; message: string }): string {
  return error.code === "FORBIDDEN" || error.code === "UNAUTHENTICATED"
    ? "Sign in as the shop owner to manage frames."
    : error.message;
}
