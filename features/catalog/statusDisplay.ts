import type { StockStatus } from "./types";

/**
 * How a stock band is worded, coloured and ordered, in one place because the
 * availability report and the low-stock banner both render the same bands. A new
 * band is one entry here, not a third copy in a second component.
 */

export const STATUS_LABEL: Record<StockStatus, string> = {
  IN_STOCK: "In stock",
  LOW_STOCK: "Low stock",
  OUT_OF_STOCK: "Out of stock",
};

export const STATUS_BADGE: Record<StockStatus, string> = {
  IN_STOCK: "bg-emerald-500/15 text-emerald-700 dark:text-emerald-400",
  LOW_STOCK: "bg-amber-500/15 text-amber-700 dark:text-amber-400",
  OUT_OF_STOCK: "bg-red-500/15 text-red-700 dark:text-red-400",
};

/** Out of stock first, then low, so the shelves to restock are at the top. */
export const STATUS_ORDER: Record<StockStatus, number> = {
  OUT_OF_STOCK: 0,
  LOW_STOCK: 1,
  IN_STOCK: 2,
};
