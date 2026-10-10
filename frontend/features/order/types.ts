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
