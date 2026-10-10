import { apiPostForm } from "@/lib/api/client";
import type { ApiResult } from "@/types/api";
import type { Prescription, PrescriptionSubmission } from "./types";

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
