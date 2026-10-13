"use client";

import Image from "next/image";
import { useEffect, useState } from "react";
import { listNewArrivalsFrames, listNewArrivalsLenses } from "../api";
import type { StorefrontFrame, StorefrontLens } from "../types";

const PLACEHOLDER_IMAGE = "/images/frames/placeholder.svg";
const IMAGE_SIZES = "(min-width: 1024px) 25vw, (min-width: 640px) 40vw, 90vw";
const PRICE_FORMAT = new Intl.NumberFormat("en-LK", {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

export default function NewArrivals() {
  const [frames, setFrames] = useState<StorefrontFrame[]>([]);
  const [lenses, setLenses] = useState<StorefrontLens[]>([]);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([listNewArrivalsFrames(4), listNewArrivalsLenses(4)])
      .then(([fRes, lRes]) => {
        if (!fRes.ok && !lRes.ok) {
          setError("Failed to load new arrivals");
          return;
        }
        setFrames(fRes.ok ? fRes.data : []);
        setLenses(lRes.ok ? lRes.data : []);
        setError(null);
      })
      .catch(() => setError("Failed to load new arrivals"));
  }, []);

  if (error) return <p className="text-sm text-red-600">{error}</p>;
  if (frames.length === 0 && lenses.length === 0) return null;

  return (
    <section className="flex flex-col gap-6">
      <h2 className="text-xl font-semibold">New Arrivals</h2>
      {frames.length > 0 && (
        <div className="flex flex-col gap-4">
          <h3 className="text-sm font-medium uppercase opacity-70">Frames</h3>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {frames.map((frame) => (
              <li
                key={`f-${frame.id}`}
                className="rounded-lg border border-black/10 dark:border-white/15"
              >
                <a href={`/frames/${frame.id}`} className="flex flex-col">
                  <div className="relative aspect-[4/3] bg-black/5">
                    <Image
                      src={frame.imageUrl || PLACEHOLDER_IMAGE}
                      alt={frame.model}
                      fill
                      sizes={IMAGE_SIZES}
                      className="object-cover"
                      onError={(e) => {
                        const img = e.target as HTMLImageElement;
                        img.src = PLACEHOLDER_IMAGE;
                      }}
                    />
                  </div>
                  <div className="p-3">
                    <p className="text-sm font-medium">{frame.model}</p>
                    <p className="text-xs opacity-70">
                      {[frame.color, frame.material]
                        .filter(Boolean)
                        .join(" · ")}
                    </p>
                    <p className="mt-1 text-sm">
                      Rs{" "}
                      {PRICE_FORMAT.format(
                        typeof frame.price === "number"
                          ? frame.price
                          : Number(frame.price),
                      )}
                    </p>
                  </div>
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
      {lenses.length > 0 && (
        <div className="flex flex-col gap-4">
          <h3 className="text-sm font-medium uppercase opacity-70">Lenses</h3>
          <ul className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-4">
            {lenses.map((lens) => (
              <li
                key={`l-${lens.id}`}
                className="rounded-lg border border-black/10 dark:border-white/15"
              >
                <a href={`/lenses/${lens.id}`} className="flex flex-col">
                  <div className="relative aspect-[4/3] bg-black/5">
                    <Image
                      src={lens.imageUrl || PLACEHOLDER_IMAGE}
                      alt={lens.name}
                      fill
                      sizes={IMAGE_SIZES}
                      className="object-cover"
                      onError={(e) => {
                        const img = e.target as HTMLImageElement;
                        img.src = PLACEHOLDER_IMAGE;
                      }}
                    />
                  </div>
                  <div className="p-3">
                    <p className="text-sm font-medium">{lens.name}</p>
                    <p className="text-xs opacity-70">
                      {[lens.type, lens.coating].filter(Boolean).join(" · ")}
                    </p>
                    <p className="mt-1 text-sm">
                      Rs{" "}
                      {PRICE_FORMAT.format(
                        typeof lens.price === "number"
                          ? lens.price
                          : Number(lens.price),
                      )}
                    </p>
                  </div>
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
}
