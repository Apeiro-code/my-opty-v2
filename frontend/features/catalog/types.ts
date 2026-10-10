/**
 * Mirrors the catalog module's frame and lens DTOs. Keep in step with
 * `backend/catalog` (`CreateFrameRequest`, `UpdateFrameRequest`, `FrameResponse`,
 * `CreateLensRequest`, `LensResponse`).
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

/**
 * The kind of correction a lens provides. Mirrors the catalog module's
 * `LensType`, which in turn mirrors `lens.type`'s `chk_lens_type` constraint
 * (V4) and order's `OrderType`.
 */
export type LensType = "SINGLE_VISION" | "BIFOCAL" | "PROGRESSIVE";

/** Mirrors the catalog module's `LensResponse`. */
export type Lens = {
  id: number;
  name: string;
  type: LensType;
  coating: string | null;
  price: number;
  stockQty: number;
  /** The shop's control over the collection; an inactive lens is kept, not deleted. */
  active: boolean;
};

/**
 * The body of `POST /api/shop/lenses`: the details of a new lens. `name` and
 * `type` are required; `coating` is nullable because a lens may be sold
 * uncoated.
 */
export type LensInput = {
  name: string;
  type: LensType;
  coating: string | null;
  price: number;
  stockQty: number;
  active: boolean;
};
