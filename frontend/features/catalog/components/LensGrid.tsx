"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { listStorefrontLenses } from "../api";
import type { StorefrontLens } from "../types";
import { computeBestDiscount, useDiscounts } from "@/features/discounts/hooks";

const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";

const IMAGE_SIZES = "(min-width: 1024px) 30vw, (min-width: 640px) 45vw, 90vw";

/**
 * The public lenses list: every active lens on offer, in a grid a visitor can
 * browse.
 */
export default function LensGrid() {
  const [lenses, setLenses] = useState<StorefrontLens[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [type] = useState("");

  const { discounts } = useDiscounts();

  useEffect(() => {
    let active = true;
    listStorefrontLenses({ type: type || null })
      .then((res) => {
        if (!active) {
          return;
        }
        if (!res.ok) {
          setLoadError(res.error.message);
          setLenses(null);
          return;
        }
        setLoadError(null);
        setLenses(res.data);
      })
      .catch((error: unknown) => {
        if (!active) {
          return;
        }
        setLoadError(
          error instanceof Error ? error.message : "Failed to load lenses",
        );
        setLenses(null);
      });
    return () => {
      active = false;
    };
  }, [type]);

  if (loadError) {
    return (
      <p role="alert" className="text-sm text-red-600 dark:text-red-400">
        {loadError}
      </p>
    );
  }

  if (lenses === null) {
    return <p className="text-sm opacity-70">Loading…</p>;
  }

  if (lenses.length === 0) {
    return (
      <p className="text-sm opacity-70">
        No lenses are available yet. Check back soon.
      </p>
    );
  }

  return (
    <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {lenses.map((lens) => (
        <LensCard key={lens.id} lens={lens} discounts={discounts} />
      ))}
    </ul>
  );
}

function LensCard({
  lens,
  discounts,
}: {
  lens: StorefrontLens;
  discounts: import("@/features/discounts/types").Discount[];
}) {
  const [imageFailed, setImageFailed] = useState(false);
  const image = lens.imageUrl;
  const best = computeBestDiscount(lens.price, lens.id, discounts);
  const base = typeof lens.price === "number" ? lens.price : Number(lens.price);

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
            alt={lens.name}
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
            <a href={`/lenses/${lens.id}`} className="hover:underline">
              {lens.name}
            </a>
          </h2>
          <span
            className={
              lens.inStock
                ? "rounded bg-green-600/10 px-2 py-0.5 text-xs text-green-700 dark:text-green-400"
                : "rounded bg-red-600/10 px-2 py-0.5 text-xs text-red-700 dark:text-red-400"
            }
          >
            {lens.inStock ? "In stock" : "Out of stock"}
          </span>
        </div>
        <p className="text-sm opacity-80">
          {[lens.type, lens.coating].filter(Boolean).join(" · ")}
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
