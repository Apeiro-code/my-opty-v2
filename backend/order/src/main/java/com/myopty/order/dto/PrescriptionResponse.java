package com.myopty.order.dto;

import com.myopty.order.model.Prescription;
import com.myopty.order.model.VerificationStatus;

/**
 * What the customer gets back after submitting: the stored prescription, minus
 * anything they have no use for.
 *
 * <p>The object key is reported as {@code hasDocument} rather than verbatim. The
 * key is how the server finds the bytes; handing it out would invite a caller to
 * treat it as a URL, and the download endpoint that owns that job (a later story)
 * will not need it either.
 *
 * <p>{@code rejectionReason} is null until a rejection writes it; the global
 * {@code non_null} inclusion drops it from the JSON while it is unset, so the
 * customer's submit response is unchanged. When set, it is exactly the "what is
 * missing" the customer needs to fix.
 */
public record PrescriptionResponse(
        long id,
        String sphLeft,
        String sphRight,
        String cylLeft,
        String cylRight,
        String axisLeft,
        String axisRight,
        String addPowerLeft,
        String addPowerRight,
        boolean progressive,
        boolean hasDocument,
        String documentContentType,
        VerificationStatus verificationStatus,
        String rejectionReason) {

    public static PrescriptionResponse from(Prescription prescription) {
        return new PrescriptionResponse(
                prescription.getId(),
                prescription.getSphLeft(),
                prescription.getSphRight(),
                prescription.getCylLeft(),
                prescription.getCylRight(),
                prescription.getAxisLeft(),
                prescription.getAxisRight(),
                prescription.getAddPowerLeft(),
                prescription.getAddPowerRight(),
                prescription.isProgressive(),
                prescription.getDocumentObjectKey() != null,
                prescription.getDocumentContentType(),
                prescription.getVerificationStatus(),
                prescription.getRejectionReason());
    }
}
