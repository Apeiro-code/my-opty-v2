/**
 * Mirrors the catalog module's frame DTOs. Keep in step with `backend/catalog`
 * (`CreateFrameRequest`, `UpdateFrameRequest`, `FrameResponse`).
 */

/** Mirrors the catalog module's `FrameResponse`. */
export type Frame = {
  id: number;
  model: string;
  color: string | null;
  material: string;
  price: number;
  stockQty: number;
  /** The shop's control over the shop wall; an inactive frame is kept, not deleted. */
  active: boolean;
};

/**
 * The body of `POST /api/shop/frames` and `PUT /api/shop/frames/{id}`. The two
 * requests carry the same fields, so one type serves both; `color` is nullable
 * because it is a variant rather than an identity.
 */
export type FrameInput = {
  model: string;
  color: string | null;
  material: string;
  price: number;
  stockQty: number;
  active: boolean;
};
