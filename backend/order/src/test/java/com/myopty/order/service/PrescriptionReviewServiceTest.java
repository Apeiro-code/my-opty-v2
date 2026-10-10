package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.exception.InvalidStateException;
import com.myopty.order.exception.ResourceNotFoundException;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * The shop's prescription review rules, without a database: what verify and reject
 * write, that a decision is one-way, and what the default queue holds.
 *
 * <p>The repository is a hand-written fake because what is under test is the shape
 * of the row the service builds and the checks it makes before building it, which a
 * fake keeps readable.
 */
class PrescriptionReviewServiceTest {

    private final FakePrescriptionRepository repository = new FakePrescriptionRepository();
    private final PrescriptionReviewService service = new PrescriptionReviewService(repository);

    private static Prescription prescription(long id, VerificationStatus status) {
        Prescription prescription = new Prescription();
        prescription.setId(id);
        prescription.setCustomerId(7L);
        prescription.setVerificationStatus(status);
        return prescription;
    }

    @Test
    void verifyingAPendingPrescriptionStampsTheDecisionAndTheClient() {
        repository.put(prescription(4L, VerificationStatus.PENDING_REVIEW));

        PrescriptionResponse response = service.verify(9L, 4L);

        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.VERIFIED);
        assertThat(response.rejectionReason()).isNull();
        assertThat(repository.saved.getVerifiedBy()).isEqualTo(9L);
        assertThat(repository.saved.getVerifiedAt()).isNotNull();
    }

    @Test
    void rejectingStoresTheReasonAndTheClient() {
        repository.put(prescription(4L, VerificationStatus.PENDING_REVIEW));

        PrescriptionResponse response = service.reject(9L, 4L, "  Add power is missing.  ");

        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.REJECTED);
        assertThat(response.rejectionReason()).isEqualTo("Add power is missing.");
        assertThat(repository.saved.getVerifiedBy()).isEqualTo(9L);
        assertThat(repository.saved.getVerifiedAt()).isNotNull();
    }

    @Test
    void anAlreadyVerifiedPrescriptionCannotBeReviewedAgain() {
        repository.put(prescription(4L, VerificationStatus.VERIFIED));

        assertThatThrownBy(() -> service.verify(9L, 4L)).isInstanceOf(InvalidStateException.class);
        assertThatThrownBy(() -> service.reject(9L, 4L, "changed my mind")).isInstanceOf(InvalidStateException.class);
        assertThat(repository.saved).isNull();
    }

    @Test
    void anAlreadyRejectedPrescriptionCannotBeReviewedAgain() {
        repository.put(prescription(4L, VerificationStatus.REJECTED));

        assertThatThrownBy(() -> service.verify(9L, 4L)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void reviewingAPrescriptionThatDoesNotExistIsNotFound() {
        assertThatThrownBy(() -> service.verify(9L, 404L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theQueueDefaultsToPendingReviewOldestFirst() {
        repository.put(prescription(3L, VerificationStatus.PENDING_REVIEW));
        repository.put(prescription(2L, VerificationStatus.VERIFIED));
        repository.put(prescription(1L, VerificationStatus.PENDING_REVIEW));

        assertThat(service.queue(null)).extracting(PrescriptionResponse::id).containsExactly(1L, 3L);
    }

    @Test
    void anExplicitStatusCollectsThatHistory() {
        repository.put(prescription(2L, VerificationStatus.VERIFIED));
        repository.put(prescription(1L, VerificationStatus.PENDING_REVIEW));

        assertThat(service.queue(VerificationStatus.VERIFIED))
                .extracting(PrescriptionResponse::id)
                .containsExactly(2L);
    }

    private static final class FakePrescriptionRepository implements PrescriptionRepository {

        private final Map<Long, Prescription> byId = new HashMap<>();
        private Prescription saved;

        void put(Prescription prescription) {
            byId.put(prescription.getId(), prescription);
        }

        @Override
        public Prescription save(Prescription prescription) {
            this.saved = prescription;
            byId.put(prescription.getId(), prescription);
            return prescription;
        }

        @Override
        public Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public List<Prescription> findAllByCustomerIdOrderByIdDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the review story");
        }

        @Override
        public Optional<Prescription> findById(Long id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public List<Prescription> findAllByVerificationStatusOrderByIdAsc(VerificationStatus verificationStatus) {
            return byId.values().stream()
                    .filter(prescription -> prescription.getVerificationStatus() == verificationStatus)
                    .sorted(Comparator.comparing(Prescription::getId))
                    .toList();
        }
    }
}
