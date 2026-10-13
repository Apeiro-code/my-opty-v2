import type { StorefrontFrame, StorefrontLens, StorefrontLensDetail } from "./types";
import {
  apiDelete,
  apiDownload,
  apiGet,
  apiPost,
  apiPut,
} from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type {
  AvailabilityItem,
  AvailabilityReport,
  Category,
  Frame,
  FrameInput,
  Lens,
  LensInput,
} from "./types";

/**
 * The shop owner's frame, lens and category catalogue. The shop owner is a
 * `CLIENT`, so these calls go to `/api/shop/**`, which the backend restricts to
 * that role.
 *
 * <p>{@link listStorefrontFrames} is the exception: it reads the public
 * `/api/frames`, which the storefront's anonymous visitors use to browse.
 */

/** Every filing category, oldest first, for the frame and lens forms. */
export function listShopCategories(): Promise<ApiResult<Category[]>> {
  return apiGet<Category[]>("/api/shop/categories");
}

/** Every frame, oldest first, for the inventory list. */
export function listShopFrames(): Promise<ApiResult<Frame[]>> {
  return apiGet<Frame[]>("/api/shop/frames");
}

/**
 * Every active frame, oldest first, for the public shop wall. Anonymous: it reads
 * `/api/frames`, which needs no session. Each frame carries only what a visitor
 * may see — the friendly `inStock` flag and a thumbnail, not the shop's count.
 */
export function listStorefrontFrames(params?: {
  category?: string;
  minPrice?: string | number | null;
  maxPrice?: string | number | null;
  color?: string | null;
  material?: string | null;
  q?: string | null;
}): Promise<ApiResult<StorefrontFrame[]>> {
  const clean: Record<string, string | number | boolean | undefined> = {};
  if (params?.category) {
    clean.category = params.category;
  }
  if (
    params?.minPrice !== undefined &&
    params?.minPrice !== null &&
    String(params.minPrice).trim() !== ""
  ) {
    clean.minPrice = params.minPrice;
  }
  if (
    params?.maxPrice !== undefined &&
    params?.maxPrice !== null &&
    String(params.maxPrice).trim() !== ""
  ) {
    clean.maxPrice = params.maxPrice;
  }
  if (
    params?.color !== undefined &&
    params?.color !== null &&
    params.color.trim() !== ""
  ) {
    clean.color = params.color.trim();
  }
  if (
    params?.material !== undefined &&
    params?.material !== null &&
    params.material.trim() !== ""
  ) {
    clean.material = params.material.trim();
  }
  if (params?.q && params.q.trim() !== "") {
    clean.q = params.q.trim();
  }
  return apiGet<StorefrontFrame[]>("/api/frames", clean);
}

export function getStorefrontFrame(
  id: number,
): Promise<ApiResult<StorefrontFrame>> {
  return apiGet<StorefrontFrame>(`/api/frames/${id}`);
}

export function listStorefrontLenses(params?: {
  type?: string | null;
}): Promise<ApiResult<StorefrontLens[]>> {
  const clean: Record<string, string | undefined> = {};
  if (params?.type && params.type.trim() !== "") {
    clean.type = params.type.trim();
  }
  return apiGet<StorefrontLens[]>("/api/lenses", clean);
}

export function getStorefrontLens(id: number): Promise<ApiResult<StorefrontLensDetail>> {
  return apiGet<StorefrontLensDetail>(`/api/lenses/${id}`);
}

export function listNewArrivalsFrames(limit = 8): Promise<ApiResult<StorefrontFrame[]>> {
  return apiGet<StorefrontFrame[]>("/api/frames/new-arrivals", { limit });
}

export function listNewArrivalsLenses(limit = 8): Promise<ApiResult<StorefrontLens[]>> {
  return apiGet<StorefrontLens[]>("/api/lenses/new-arrivals", { limit });
}

/** Adds a frame so it appears on the site. */
export function createFrame(input: FrameInput): Promise<ApiResult<Frame>> {
  return apiPost<Frame>("/api/shop/frames", input);
}

/** Replaces an existing frame's details so they stay accurate. */
export function updateFrame(
  id: number,
  input: FrameInput,
): Promise<ApiResult<Frame>> {
  return apiPut<Frame>(`/api/shop/frames/${id}`, input);
}

/**
 * Takes a frame off the site. It is a discontinue, not a row delete: the frame
 * comes back hidden, so old orders still resolve and an edit can put it back.
 */
export function discontinueFrame(id: number): Promise<ApiResult<Frame>> {
  return apiDelete<Frame>(`/api/shop/frames/${id}`);
}

/** Every lens, oldest first, for the collection list. */
export function listShopLenses(): Promise<ApiResult<Lens[]>> {
  return apiGet<Lens[]>("/api/shop/lenses");
}

/** Adds a lens so it appears in the collection. */
export function createLens(input: LensInput): Promise<ApiResult<Lens>> {
  return apiPost<Lens>("/api/shop/lenses", input);
}

/** Replaces an existing lens's details so they stay accurate. */
export function updateLens(
  id: number,
  input: LensInput,
): Promise<ApiResult<Lens>> {
  return apiPut<Lens>(`/api/shop/lenses/${id}`, input);
}

/**
 * The live availability report: every active frame and lens with its current
 * stock and health band, for planning reorders. Computed on read, so calling it
 * again is what makes it real-time.
 */
export function getAvailabilityReport(): Promise<
  ApiResult<AvailabilityReport>
> {
  return apiGet<AvailabilityReport>("/api/shop/inventory/report");
}

/**
 * The items that are low or out of stock, worst first. A narrowed view of the
 * availability report — same items, only the ones that need attention — so the
 * shop can see at a glance what to reorder. The backend also emails the
 * configured address whenever a sale crosses a threshold; this is the in-app half.
 */
export function getLowStockAlerts(): Promise<ApiResult<AvailabilityItem[]>> {
  return apiGet<AvailabilityItem[]>("/api/shop/inventory/alerts");
}

/**
 * Downloads the live availability report as a file. The same rows the screen
 * shows, rendered by the backend as a PDF or an Excel workbook; nothing is
 * stored server-side. `filename` names the saved file, since the server's
 * `Content-Disposition` is not readable across origins.
 */
export function exportAvailabilityReport(
  format: "pdf" | "xlsx",
  filename: string,
): Promise<ApiResult<void>> {
  return apiDownload(
    `/api/shop/inventory/report/export?format=${format}`,
    filename,
  );
}
