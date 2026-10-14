"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { listStorefrontFrames } from "../api";
import type { StorefrontFrame } from "../types";
import { computeBestDiscount, useDiscounts } from "@/features/discounts/hooks";

const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";

const IMAGE_SIZES = "(min-width: 1024px) 30vw, (min-width: 640px) 45vw, 90vw";

/**
 * The public shop wall: every frame the shop has on sale, in a grid a visitor can
 * browse. Anonymous — it reads the public `/api/frames`, so it works logged out,
 * and the backend scopes the list to active frames and to the visitor-safe fields.
 *
 * <p>A client component because it loads on mount and responds to filter changes.
 * Each card shows the thumbnail, model, colour and material, the price and whether
 * it is in stock; a frame with no image, or one that fails to load, falls back to
 * the placeholder tile.
 */
export default function FrameGrid() {
  const [frames, setFrames] = useState<StorefrontFrame[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [category, setCategory] = useState("");
  const [minPrice, setMinPrice] = useState("");
  const [maxPrice, setMaxPrice] = useState("");
  const [color, setColor] = useState("");
  const [material, setMaterial] = useState("");
  const [debouncedColor, setDebouncedColor] = useState("");
  const [debouncedMaterial, setDebouncedMaterial] = useState("");
  const [search, setSearch] = useState("");
  const [debouncedSearch, setDebouncedSearch] = useState("");
  useEffect(() => {
    const timer = setTimeout(() => setDebouncedColor(color), 300);
    return () => clearTimeout(timer);
  }, [color]);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedMaterial(material), 300);
    return () => clearTimeout(timer);
  }, [material]);

  const { discounts } = useDiscounts();

  useEffect(() => {
    let active = true;
    listStorefrontFrames({
      category: category || undefined,
      minPrice: minPrice || undefined,
      maxPrice: maxPrice || undefined,
      color: debouncedColor || undefined,
      material: debouncedMaterial || undefined,
      q: debouncedSearch || undefined,
    }).then((result) => {
      if (!active) {
        return;
      }
      if (!result.ok) {
        setLoadError(result.error.message);
        return;
      }
      setLoadError(null);
      setFrames(result.data);
    });
    return () => {
      active = false;
    };
  }, [
    category,
    minPrice,
    maxPrice,
    debouncedColor,
    debouncedMaterial,
    debouncedSearch,
  ]);

  function clearFilters() {
    setCategory("");
    setMinPrice("");
    setMaxPrice("");
    setColor("");
    setMaterial("");
    setSearch("");
    setDebouncedColor("");
    setDebouncedMaterial("");
    setDebouncedSearch("");
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

  if (frames.length === 0) {
    return (
      <>
        <FilterControls
          category={category}
          minPrice={minPrice}
          maxPrice={maxPrice}
          color={color}
          material={material}
          onCategoryChange={setCategory}
          onMinPriceChange={setMinPrice}
          onMaxPriceChange={setMaxPrice}
          onColorChange={setColor}
          onMaterialChange={setMaterial}
          search={search}
          onSearchChange={setSearch}
          onClear={clearFilters}
          hasFilters={Boolean(
            category ||
            minPrice ||
            maxPrice ||
            debouncedColor ||
            debouncedMaterial ||
            debouncedSearch,
          )}
        />
        <p className="text-sm opacity-70">
          {Boolean(
            category ||
            minPrice ||
            maxPrice ||
            debouncedColor ||
            debouncedMaterial ||
            debouncedSearch,
          )
            ? "No frames match your filters or search. Try adjusting them."
            : "No frames are on the wall yet. Check back soon."}
        </p>
      </>
    );
  }

  return (
    <>
      <FilterControls
        category={category}
        minPrice={minPrice}
        maxPrice={maxPrice}
        color={color}
        material={material}
        onCategoryChange={setCategory}
        onMinPriceChange={setMinPrice}
        onMaxPriceChange={setMaxPrice}
        onColorChange={setColor}
        onMaterialChange={setMaterial}
        search={search}
        onSearchChange={setSearch}
        onClear={clearFilters}
        hasFilters={Boolean(
          category ||
          minPrice ||
          maxPrice ||
          debouncedColor ||
          debouncedMaterial ||
          debouncedSearch,
        )}
      />
      <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {frames.map((frame) => (
          <FrameCard key={frame.id} frame={frame} discounts={discounts} />
        ))}
      </ul>
    </>
  );
}

function FrameCard({
  frame,
  discounts,
}: {
  frame: StorefrontFrame;
  discounts: import("@/features/discounts/types").Discount[];
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const image = frame.imageUrl;
  const best = computeBestDiscount(frame.price, frame.id, discounts);
  const base =
    typeof frame.price === "number" ? frame.price : Number(frame.price);

  return (
    <li className="flex flex-col overflow-hidden rounded-lg border border-black/10 dark:border-white/15">
      <div className="relative aspect-[4/3] w-full bg-black/5 dark:bg-white/10">
        {best.hasDiscount && (
          <span className="absolute left-2 top-2 z-10 rounded bg-red-600 px-2 py-0.5 text-xs font-medium text-white">
            -{best.percentage}%
          </span>
        )}
        {image === null || imageFailed ? (
          <Image
            src={PLACEHOLDER_IMAGE}
            alt=""
            fill
            sizes={IMAGE_SIZES}
            unoptimized
            className="object-contain p-8 opacity-60"
          />
        ) : (
          <Image
            src={image}
            alt={frame.model}
            fill
            sizes={IMAGE_SIZES}
            className="object-cover"
            onError={() => setImageFailed(true)}
          />
        )}
      </div>
      <div className="flex flex-1 flex-col gap-1 p-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <h2 className="font-medium">
            <a href={`/frames/${frame.id}`} className="hover:underline">
              {frame.model}
            </a>
          </h2>
          <span
            className={
              frame.inStock
                ? "rounded bg-green-600/10 px-2 py-0.5 text-xs text-green-700 dark:text-green-400"
                : "rounded bg-black/10 px-2 py-0.5 text-xs dark:bg-white/10"
            }
          >
            {frame.inStock ? "In stock" : "Out of stock"}
          </span>
        </div>
        <p className="text-sm opacity-75">
          {[frame.color, frame.material].filter(Boolean).join(" · ")}
        </p>
        {best.hasDiscount ? (
          <div className="mt-2 flex items-baseline gap-2">
            <span className="text-sm font-medium">
              Rs {PRICE_FORMAT.format(best.effectivePrice)}
            </span>
            <span className="text-xs opacity-60 line-through">
              Rs {PRICE_FORMAT.format(base)}
            </span>
          </div>
        ) : (
          <p className="mt-2 text-sm font-medium">
            Rs {PRICE_FORMAT.format(base)}
          </p>
        )}
      </div>
    </li>
  );
}

type FilterControlsProps = {
  category: string;
  minPrice: string;
  maxPrice: string;
  color: string;
  material: string;
  search: string;
  onCategoryChange: (value: string) => void;
  onMinPriceChange: (value: string) => void;
  onMaxPriceChange: (value: string) => void;
  onColorChange: (value: string) => void;
  onMaterialChange: (value: string) => void;
  onSearchChange: (value: string) => void;
  onClear: () => void;
  hasFilters: boolean;
};

function FilterControls({
  category,
  minPrice,
  maxPrice,
  color,
  material,
  search,
  onCategoryChange,
  onMinPriceChange,
  onMaxPriceChange,
  onColorChange,
  onMaterialChange,
  onSearchChange,
  onClear,
  hasFilters,
}: FilterControlsProps) {
  return (
    <div className="flex flex-col gap-3 rounded-lg border border-black/10 p-4 dark:border-white/15">
      <div className="grid grid-cols-1 gap-3 md:grid-cols-6">
        <label className="flex flex-col gap-1 text-sm md:col-span-2">
          <span className="font-medium">Search</span>
          <input
            type="text"
            placeholder="Search by model name..."
            value={search}
            onChange={(event) => onSearchChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium">Category</span>
          <select
            value={category}
            onChange={(event) => onCategoryChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          >
            <option value="">All</option>
            <option value="men">Men</option>
            <option value="women">Women</option>
            <option value="kids">Kids</option>
          </select>
        </label>
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium">Min price</span>
          <input
            type="number"
            min="0"
            step="0.01"
            value={minPrice}
            onChange={(event) => onMinPriceChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium">Max price</span>
          <input
            type="number"
            min="0"
            step="0.01"
            value={maxPrice}
            onChange={(event) => onMaxPriceChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium">Colour</span>
          <input
            type="text"
            value={color}
            onChange={(event) => onColorChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </label>
        <label className="flex flex-col gap-1 text-sm">
          <span className="font-medium">Material</span>
          <input
            type="text"
            value={material}
            onChange={(event) => onMaterialChange(event.target.value)}
            className="rounded-md border border-black/15 bg-transparent px-2 py-1.5 text-sm dark:border-white/20"
          />
        </label>
      </div>
      {Boolean(category || minPrice || maxPrice || color || material) ? (
        <div className="flex justify-end">
          <button
            type="button"
            onClick={onClear}
            className="rounded-md border border-black/15 px-3 py-1.5 text-sm dark:border-white/20"
          >
            Clear filters
          </button>
        </div>
      ) : null}
    </div>
  );
}
