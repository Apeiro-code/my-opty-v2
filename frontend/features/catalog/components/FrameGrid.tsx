"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { listStorefrontFrames } from "../api";
import type { StorefrontFrame } from "../types";

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
 * <p>A client component because it loads on mount. Each card shows the thumbnail,
 * model, colour and material, the price and whether it is in stock; a frame with
 * no image, or one that fails to load, falls back to the placeholder tile.
 */
export default function FrameGrid() {
  const [frames, setFrames] = useState<StorefrontFrame[] | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listStorefrontFrames().then((result) => {
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
  }, []);

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
      <p className="text-sm opacity-70">
        No frames are on the wall yet. Check back soon.
      </p>
    );
  }

  return (
    <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {frames.map((frame) => (
        <FrameCard key={frame.id} frame={frame} />
      ))}
    </ul>
  );
}

function FrameCard({ frame }: { frame: StorefrontFrame }) {
  const [imageFailed, setImageFailed] = useState(false);
  const image = frame.imageUrl;

  return (
    <li className="flex flex-col overflow-hidden rounded-lg border border-black/10 dark:border-white/15">
      <div className="relative aspect-[4/3] w-full bg-black/5 dark:bg-white/10">
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
          <h2 className="font-medium">{frame.model}</h2>
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
        <p className="mt-2 text-sm font-medium">
          Rs {PRICE_FORMAT.format(frame.price)}
        </p>
      </div>
    </li>
  );
}
