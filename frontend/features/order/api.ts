import { apiGet, apiPost, apiPostForm } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type {
  CreateOrder,
  Order,
  Prescription,
  PrescriptionSubmission,
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
