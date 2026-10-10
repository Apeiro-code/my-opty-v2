import { apiGet, apiPost, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { Frame, FrameInput } from "./types";

/**
 * The shop owner's frame catalogue. The shop owner is a `CLIENT`, so these calls
 * go to `/api/shop/**`, which the backend restricts to that role.
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
