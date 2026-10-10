package com.myopty.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.myopty.order.config.ObjectStorageProperties;
import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.dto.PrescriptionSubmission;
import com.myopty.order.exception.InvalidDocumentException;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

/**
 * The upload rules, without a database or a disk: what the store is asked to keep,
 * what it is told to forget when the insert fails, and which documents are refused
 * before any of that happens.
 *
 * <p>The repository and store are hand-written fakes rather than mocks because
 * what is under test is the sequence of calls — store then insert, and delete
 * again on failure — and a recording fake makes that sequence readable in the
 * assertion instead of buried in verification plumbing.
 */
class PrescriptionServiceTest {

    private final RecordingRepository repository = new RecordingRepository();
    private final RecordingObjectStore store = new RecordingObjectStore();
    private final PrescriptionService service =
            new PrescriptionService(repository, store, new ObjectStorageProperties());

    private static PrescriptionSubmission submission() {
        return new PrescriptionSubmission("+06.25", "+05.50", "-1.75", "-1.50", "180", "175", "+2.00", "+2.00", true);
    }

    private static MockMultipartFile document(String contentType) {
        return new MockMultipartFile("document", "scan", contentType, "bytes".getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void aSubmissionWithoutADocumentIsStoredAndPendingReview() {
        PrescriptionResponse response = service.submit(7L, submission(), null);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.hasDocument()).isFalse();
        assertThat(response.verificationStatus()).isEqualTo(VerificationStatus.PENDING_REVIEW);
        assertThat(repository.saved.getDocumentObjectKey()).isNull();
        assertThat(store.puts).isEmpty();
    }

    @Test
    void aDocumentIsStoredAndItsKeyRecorded() {
        PrescriptionResponse response = service.submit(7L, submission(), document("application/pdf"));

        assertThat(response.hasDocument()).isTrue();
        assertThat(response.documentContentType()).isEqualTo("application/pdf");
        assertThat(store.puts).singleElement().satisfies(put -> {
            assertThat(put.key()).isEqualTo(repository.saved.getDocumentObjectKey());
            assertThat(put.contentType()).isEqualTo("application/pdf");
        });
    }

    @Test
    void aContentTypeWithParametersAndAJPEGAliasIsAccepted() {
        service.submit(7L, submission(), document("image/jpg; charset=binary"));

        assertThat(repository.saved.getDocumentContentType()).isEqualTo("image/jpeg");
        // The canonical type, not the alias, decides the stored key's extension.
        assertThat(repository.saved.getDocumentObjectKey()).endsWith(".jpg");
    }

    @Test
    void anUnsupportedDocumentTypeIsRefusedBeforeAnythingIsStored() {
        assertThatThrownBy(() -> service.submit(7L, submission(), document("application/zip")))
                .isInstanceOf(InvalidDocumentException.class);
        assertThat(store.puts).isEmpty();
        assertThat(repository.saved).isNull();
    }

    @Test
    void aDocumentOverTheCapIsRefused() {
        byte[] tooBig =
                new byte[(int) new ObjectStorageProperties().getMaxFileSize().toBytes() + 1];
        MockMultipartFile oversized = new MockMultipartFile("document", "scan.pdf", "application/pdf", tooBig);

        assertThatThrownBy(() -> service.submit(7L, submission(), oversized))
                .isInstanceOf(InvalidDocumentException.class);
        assertThat(store.puts).isEmpty();
    }

    @Test
    void aFailedInsertDeletesTheDocumentItJustStored() {
        repository.failOnSave = true;

        assertThatThrownBy(() -> service.submit(7L, submission(), document("application/pdf")))
                .isInstanceOf(IllegalStateException.class);

        // The row never landed, so the bytes must not be left behind.
        assertThat(store.deletes).containsExactly(store.puts.get(0).key());
    }

    private static final class RecordingRepository implements PrescriptionRepository {

        private final AtomicLong ids = new AtomicLong();
        private Prescription saved;
        private boolean failOnSave;

        @Override
        public Prescription save(Prescription prescription) {
            if (failOnSave) {
                throw new IllegalStateException("insert failed");
            }
            prescription.setId(ids.incrementAndGet());
            this.saved = prescription;
            return prescription;
        }

        @Override
        public Optional<Prescription> findByIdAndCustomerId(Long id, Long customerId) {
            throw new UnsupportedOperationException("not used by the submit story");
        }

        @Override
        public List<Prescription> findAllByCustomerIdOrderByIdDesc(Long customerId) {
            throw new UnsupportedOperationException("not used by the submit story");
        }
    }

    private static final class RecordingObjectStore implements ObjectStore {

        private final List<PutCall> puts = new ArrayList<>();
        private final List<String> deletes = new ArrayList<>();

        @Override
        public void put(String key, InputStream content, long contentLength, String contentType) {
            puts.add(new PutCall(key, contentType));
        }

        @Override
        public void delete(String key) {
            deletes.add(key);
        }
    }

    private record PutCall(String key, String contentType) {}
}
