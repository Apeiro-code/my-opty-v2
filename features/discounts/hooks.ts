"use client";

import { useEffect, useState } from "react";
import { getActiveDiscounts } from "./api";
import type { Discount } from "./types";

export type BestDiscount = {
  hasDiscount: boolean;
  percentage: number;
  description: string | null;
  effectivePrice: number;
};

function isActiveDiscount(d: Discount): boolean {
  return d.state === "ACTIVE";
}

function findBestDiscount(
  discounts: Discount[],
  itemId: number,
): Discount | null {
  const active = discounts.filter(isActiveDiscount);
  let best: Discount | null = null;
  for (const d of active) {
    if (d.targetItemIds && d.targetItemIds.includes(itemId)) {
      if (!best || d.percentage > best.percentage) {
        best = d;
      }
    }
  }
  if (best) return best;
  const sitewide = active.find((d) => d.sitewide);
  if (sitewide) return sitewide;
  return null;
}

export function computeBestDiscount(
  price: number | string,
  itemId: number,
  discounts: Discount[],
): BestDiscount {
  const base = typeof price === "number" ? price : Number(price);
  const best = findBestDiscount(discounts, itemId);
  if (best) {
    const pct = Number(best.percentage);
    const effective = base * (1 - pct / 100);
    return {
      hasDiscount: true,
      percentage: pct,
      description: best.description,
      effectivePrice: Math.round(effective * 100) / 100,
    };
  }
  return {
    hasDiscount: false,
    percentage: 0,
    description: null,
    effectivePrice: base,
  };
}

export function useDiscounts() {
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    getActiveDiscounts()
      .then((res) => {
        if (!active) return;
        if (!res.ok) {
          setError(res.error.message);
          setDiscounts([]);
          setLoading(false);
          return;
        }
        setDiscounts(res.data);
        setError(null);
        setLoading(false);
      })
      .catch((e) => {
        if (!active) return;
        setError(e instanceof Error ? e.message : "Failed to load discounts");
        setDiscounts([]);
        setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  return { discounts, loading, error };
}
