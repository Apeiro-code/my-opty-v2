package com.myopty.order.service;

import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The shop's side of a prescription: the queue to review and the two decisions it
 * can end in.
 *
 * <p>Reviewing is one-way. A prescription that is already {@code VERIFIED} or
 * {@code REJECTED} cannot be decided again, so the shop cannot silently overturn
 * its own call; a customer who needs a change submits a new prescription. That is
 * why both decisions require {@link VerificationStatus#PENDING_REVIEW} before they
 * write.
 *
 * <p>{@link #reject} is what "flag missing details" means: the reason is stored
 * and travels back to the customer, so a rejection names what is wrong rather than
 * only that something is. The deciding client is stamped on {@code verified_by},
 * and the time on {@code verified_at}, so a decision can always be traced.
 */
@Service
public class PrescriptionReviewService {

    /** The README's cap on queue endpoints: at most 100 rows, oldest first. */
    private static final int MAX_QUEUE_SIZE = 100;

    private final PrescriptionRepository repository;

    public PrescriptionReviewService(PrescriptionRepository repository) {
        this.repository = repository;
    }

    /**
     * The prescriptions awaiting a decision, oldest first. A null {@code status}
     * defaults to the queue the shop actually works from, {@code PENDING_REVIEW};
     * any other value collects the history of that decision.
     */
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> queue(VerificationStatus status) {
        VerificationStatus filter = status == null ? VerificationStatus.PENDING_REVIEW : status;
        return repository.findAllByVerificationStatusOrderByIdAsc(filter).stream()
                .limit(MAX_QUEUE_SIZE)
                .map(PrescriptionResponse::from)
                .toList();
    }

    @Transactional
    public PrescriptionResponse verify(long clientId, long prescriptionId) {
        Prescription prescription = loadPending(prescriptionId);
        prescription.setVerificationStatus(VerificationStatus.VERIFIED);
        prescription.setRejectionReason(null);
        prescription.setVerifiedBy(clientId);
        prescription.setVerifiedAt(LocalDateTime.now());
        return PrescriptionResponse.from(repository.save(prescription));
    }

    @Transactional
    public PrescriptionResponse reject(long clientId, long prescriptionId, String reason) {
        Prescription prescription = loadPending(prescriptionId);
        prescription.setVerificationStatus(VerificationStatus.REJECTED);
        prescription.setRejectionReason(reason.trim());
        prescription.setVerifiedBy(clientId);
        prescription.setVerifiedAt(LocalDateTime.now());
        return PrescriptionResponse.from(repository.save(prescription));
    }

    private Prescription loadPending(long prescriptionId) {
        Prescription prescription = repository
                .findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found."));
        if (prescription.getVerificationStatus() != VerificationStatus.PENDING_REVIEW) {
            throw new InvalidStateException("This prescription has already been reviewed.");
        }
        return prescription;
    }
}
