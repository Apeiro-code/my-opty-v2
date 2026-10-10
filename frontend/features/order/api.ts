import { apiGet, apiPost, apiPostForm, apiPut } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type {
  CreateOrder,
  Order,
  OrderNotification,
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

/** What the customer was told about one order, newest first. Shop-only. */
export function listOrderNotifications(
  id: number,
): Promise<ApiResult<OrderNotification[]>> {
  return apiGet<OrderNotification[]>(`/api/shop/orders/${id}/notifications`);
}

/** The caller's own orders, newest first, for tracking. */
export function listOwnOrders(): Promise<ApiResult<Order[]>> {
  return apiGet<Order[]>("/api/orders");
}

/** A single one of the caller's own orders, including its receive date. */
export function readOrder(id: number): Promise<ApiResult<Order>> {
  return apiGet<Order>(`/api/orders/${id}`);
}

/**
 * The caller's notifications, newest first. The backend scopes them to the
 * session's customer, so no `customerId` is ever passed.
 */
export function listOwnNotifications(
  orderId?: number,
): Promise<ApiResult<OrderNotification[]>> {
  return apiGet<OrderNotification[]>("/api/notifications", { orderId });
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

/**
 * The orders still in the shop — approved, processing or ready, oldest first.
 * Each move here emails the customer, so this is the production line.
 */
export function listOrderFulfilmentQueue(): Promise<ApiResult<Order[]>> {
  return apiGet<Order[]>("/api/shop/orders/active");
}

/** Moves an order into the lab after it has been approved. */
export function markOrderProcessing(id: number): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/processing`, {});
}

/** Marks an order ready for the customer to collect. */
export function markOrderReady(id: number): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/ready`, {});
}

/** Marks an order as having left the shop. Terminal. */
export function markOrderDispatched(id: number): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/dispatched`, {});
}

/**
 * Corrects or withdraws the estimated receive date. A null value withdraws it;
 * the backend updates the date without emailing the customer, since this is a
 * correction rather than a status change.
 */
export function updateReceiveDate(
  id: number,
  receiveDate: string | null,
): Promise<ApiResult<Order>> {
  return apiPut<Order>(`/api/shop/orders/${id}/receive-date`, { receiveDate });
}
