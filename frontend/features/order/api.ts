import { apiGet, apiPost, apiPostForm, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type {
  CreateOrder,
  Order,
  OrderStatus,
  Prescription,
  PrescriptionSubmission,
  VerificationStatus,
} from "./types";

/** The values plus the optional document, as one submission. */
export type NewPrescription = PrescriptionSubmission & {
  document?: File | null;
};

/**
 * Sends a prescription as `multipart/form-data`: the optical values as a JSON
 * part named `prescription`, the document as a part named `document`.
 *
 * <p>The values go in a `Blob` with an explicit `application/json` type rather
 * than `JSON.stringify` on its own, because the backend decodes that part with a
 * JSON converter and a part with no declared type is read as bytes.
 */
export function submitPrescription(
  input: NewPrescription,
): Promise<ApiResult<Prescription>> {
  const { document, ...values } = input;

  const form = new FormData();
  form.append(
    "prescription",
    new Blob([JSON.stringify(values)], { type: "application/json" }),
  );
  if (document) {
    form.append("document", document);
  }

  return apiPostForm<Prescription>("/api/prescriptions", form);
}

/** The caller's own prescriptions, so the order form can offer one to link. */
export function listOwnPrescriptions(): Promise<ApiResult<Prescription[]>> {
  return apiGet<Prescription[]>("/api/prescriptions");
}

/**
 * Links a prescription to a frame and lens under a chosen order type. The frame
 * is optional — a customer may order lenses only.
 */
export function createOrder(input: CreateOrder): Promise<ApiResult<Order>> {
  return apiPost<Order>("/api/orders", input);
}

/**
 * The shop's prescription review queue. The shop owner is a `CLIENT`, so these
 * calls go to `/api/shop/**`, which the backend restricts to that role.
 */
export function listPrescriptionReviewQueue(
  status?: VerificationStatus,
): Promise<ApiResult<Prescription[]>> {
  return apiGet<Prescription[]>("/api/shop/prescriptions", { status });
}

/** Confirms a pending prescription so an order built on it may be approved. */
export function verifyPrescription(
  id: number,
): Promise<ApiResult<Prescription>> {
  return apiPut<Prescription>(`/api/shop/prescriptions/${id}/verify`, {});
}

/** Flags a prescription as incomplete, telling the customer what is missing. */
export function rejectPrescription(
  id: number,
  reason: string,
): Promise<ApiResult<Prescription>> {
  return apiPut<Prescription>(`/api/shop/prescriptions/${id}/reject`, {
    reason,
  });
}

/** The shop's order approval queue. */
export function listOrderApprovalQueue(
  status?: OrderStatus,
): Promise<ApiResult<Order[]>> {
  return apiGet<Order[]>("/api/shop/orders", { status });
}

/** Lets an order proceed to production; refused until its prescription is verified. */
export function approveOrder(id: number): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/approve`, {});
}

/** Stops an order, telling the customer why. */
export function rejectOrder(
  id: number,
  reason: string,
): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/reject`, { reason });
}
