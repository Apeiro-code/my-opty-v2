/**
 * Mirrors the catalog module's frame, lens and category DTOs. Keep in step with
 * `backend/catalog` (`CreateFrameRequest`, `UpdateFrameRequest`, `FrameResponse`,
 * `CreateLensRequest`, `UpdateLensRequest`, `LensResponse`, `CategoryResponse`).
 */

/** Mirrors the catalog module's `CategoryResponse`. */
export type Category = {
  id: number;
  name: string;
  slug: string;
  /** Which product a category may hold; `BOTH` fits frames and lenses. */
  itemType: CategoryItemType;
};

/**
 * What a browsing category may hold. Mirrors the catalog module's
 * `CategoryItemType`, which in turn mirrors `category.item_type`'s
 * `chk_category_item_type` constraint (V2).
 */
export type CategoryItemType = "FRAME" | "LENS" | "BOTH";

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
  /** The category it is filed under, or null while unfiled. */
  categoryId: number | null;
};

/**
 * The body of `POST /api/shop/frames` and `PUT /api/shop/frames/{id}`. The two
 * requests carry the same fields, so one type serves both; `color` is nullable
 * because it is a variant rather than an identity, and `categoryId` is nullable
 * because a frame may be left unfiled.
 */
export type FrameInput = {
  model: string;
  color: string | null;
  material: string;
  price: number;
  stockQty: number;
  active: boolean;
  categoryId: number | null;
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
  /** The category it is filed under, or null while unfiled. */
  categoryId: number | null;
};

/**
 * The body of `POST /api/shop/lenses` and `PUT /api/shop/lenses/{id}`. The two
 * requests carry the same fields, so one type serves both; `coating` is nullable
 * because a lens may be sold uncoated, and `categoryId` is nullable because a
 * lens may be left unfiled.
 */
export type LensInput = {
  name: string;
  type: LensType;
  coating: string | null;
  price: number;
  stockQty: number;
  active: boolean;
  categoryId: number | null;
};
