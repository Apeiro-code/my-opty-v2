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
 */

/** Every filing category, oldest first, for the frame and lens forms. */
export function listShopCategories(): Promise<ApiResult<Category[]>> {
  return apiGet<Category[]>("/api/shop/categories");
}

/** Every frame, oldest first, for the inventory list. */
export function listShopFrames(): Promise<ApiResult<Frame[]>> {
  return apiGet<Frame[]>("/api/shop/frames");
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
