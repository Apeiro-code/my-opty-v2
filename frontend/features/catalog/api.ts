import { apiDelete, apiGet, apiPost, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { Frame, FrameInput, Lens, LensInput } from "./types";

/**
 * The shop owner's frame and lens catalogue. The shop owner is a `CLIENT`, so
 * these calls go to `/api/shop/**`, which the backend restricts to that role.
 */

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
