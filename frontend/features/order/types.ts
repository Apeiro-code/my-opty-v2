/**
 * Mirrors the order module's prescription DTOs. Keep in step with
 * `backend/order` (`PrescriptionSubmission`, `PrescriptionResponse`).
 */

export type VerificationStatus = "PENDING_REVIEW" | "VERIFIED" | "REJECTED";

/**
 * The `prescription` part of the multipart submission. Every optical value is
 * optional because a prescription legitimately omits them; the backend refuses
 * only a submission with none at all.
 */
export type PrescriptionSubmission = {
  sphLeft?: string;
  sphRight?: string;
  cylLeft?: string;
  cylRight?: string;
  axisLeft?: string;
  axisRight?: string;
  addPowerLeft?: string;
  addPowerRight?: string;
  progressive: boolean;
};

export type Prescription = {
  id: number;
  sphLeft: string | null;
  sphRight: string | null;
  cylLeft: string | null;
  cylRight: string | null;
  axisLeft: string | null;
  axisRight: string | null;
  addPowerLeft: string | null;
  addPowerRight: string | null;
  progressive: boolean;
  hasDocument: boolean;
  documentContentType: string | null;
  verificationStatus: VerificationStatus;
};

/**
 * Mirrors the order module's `OrderType` enum, which is also `lens.type` in the
 * catalog. The selected value is the order's routing key.
 */
export type OrderType = "SINGLE_VISION" | "BIFOCAL" | "PROGRESSIVE";

/** Mirrors the order module's `OrderStatus` enum. Only `PENDING` is written today. */
export type OrderStatus =
  "PENDING" | "APPROVED" | "PROCESSING" | "READY" | "DISPATCHED" | "REJECTED";

/** The body of `POST /api/orders`. The customer comes from the session. */
export type CreateOrder = {
  prescriptionId: number;
  orderType: OrderType;
  lensId: number;
  frameId?: number | null;
  quantity?: number;
};

/** Mirrors the order module's `OrderResponse`. */
export type Order = {
  id: number;
  orderNumber: string;
  orderType: OrderType;
  status: OrderStatus;
  prescriptionId: number;
  frameId: number | null;
  lensId: number;
  quantity: number;
  totalAmount: number | null;
  orderDate: string;
};
