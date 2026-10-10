package com.myopty.order.service;

import com.myopty.order.config.ObjectStorageProperties;
import com.myopty.order.dto.PrescriptionResponse;
import com.myopty.order.dto.PrescriptionSubmission;
import com.myopty.order.exception.InvalidDocumentException;
import com.myopty.order.exception.ObjectStoreException;
import com.myopty.order.model.Prescription;
import com.myopty.order.model.VerificationStatus;
import com.myopty.order.repository.PrescriptionRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Takes a customer's prescription and its optional document, stores both, and
 * reports what was saved.
 *
 * <p>The two writes are not one transaction — one is a row, the other is a file —
 * so the service stores the document first and deletes it again if the row insert
 * fails. The opposite order would be worse: a committed row pointing at bytes
 * that were never written is a prescription the shop can read but not verify,
 * while the failure here leaves nothing committed at all.
 */
@Service
public class PrescriptionService {

    /** A few browsers and scanners still send this alias for a JPEG. */
    private static final Map<String, String> CONTENT_TYPE_ALIASES = Map.of("image/jpg", "image/jpeg");

    private static final Map<String, String> EXTENSION_FOR_CONTENT_TYPE =
            Map.of("application/pdf", ".pdf", "image/jpeg", ".jpg", "image/png", ".png");

    private final PrescriptionRepository repository;
    private final ObjectStore objectStore;
    private final ObjectStorageProperties properties;

    public PrescriptionService(
            PrescriptionRepository repository, ObjectStore objectStore, ObjectStorageProperties properties) {
        this.repository = repository;
        this.objectStore = objectStore;
        this.properties = properties;
    }

    @Transactional
    public PrescriptionResponse submit(long customerId, PrescriptionSubmission submission, MultipartFile document) {
        String objectKey = null;
        String contentType = null;
        if (document != null) {
            contentType = normaliseContentType(document.getContentType());
            validateDocument(document, contentType);
            objectKey = objectKeyFor(contentType);
            store(objectKey, document, contentType);
        }

        Prescription prescription = toEntity(customerId, submission, objectKey, contentType);
        try {
            return PrescriptionResponse.from(repository.save(prescription));
        } catch (RuntimeException exception) {
            // The row never landed, so the bytes must not linger and later look
            // like a document for a prescription that does not exist.
            if (objectKey != null) {
                objectStore.delete(objectKey);
            }
            throw exception;
        }
    }

    /**
     * The customer's own prescriptions, newest first, so the order form can offer
     * one to link. The query is scoped by customer id, so it cannot list anyone
     * else's.
     */
    @Transactional(readOnly = true)
    public List<PrescriptionResponse> listFor(long customerId) {
        return repository.findAllByCustomerIdOrderByIdDesc(customerId).stream()
                .map(PrescriptionResponse::from)
                .toList();
    }

    private void validateDocument(MultipartFile document, String contentType) {
        if (document.isEmpty()) {
            throw new InvalidDocumentException("The uploaded document is empty.");
        }
        if (!properties.getAllowedContentTypes().contains(contentType)) {
            throw new InvalidDocumentException("Upload a PDF, JPEG or PNG.");
        }
        if (document.getSize() > properties.getMaxFileSize().toBytes()) {
            throw new InvalidDocumentException("The document is larger than the allowed size.");
        }
    }

    private void store(String objectKey, MultipartFile document, String contentType) {
        try (InputStream content = document.getInputStream()) {
            objectStore.put(objectKey, content, document.getSize(), contentType);
        } catch (IOException exception) {
            throw new ObjectStoreException("Could not read the uploaded document.", exception);
        }
    }

    private Prescription toEntity(
            long customerId, PrescriptionSubmission submission, String objectKey, String contentType) {
        Prescription prescription = new Prescription();
        prescription.setCustomerId(customerId);
        prescription.setSphLeft(blankToNull(submission.sphLeft()));
        prescription.setSphRight(blankToNull(submission.sphRight()));
        prescription.setCylLeft(blankToNull(submission.cylLeft()));
        prescription.setCylRight(blankToNull(submission.cylRight()));
        prescription.setAxisLeft(blankToNull(submission.axisLeft()));
        prescription.setAxisRight(blankToNull(submission.axisRight()));
        prescription.setAddPowerLeft(blankToNull(submission.addPowerLeft()));
        prescription.setAddPowerRight(blankToNull(submission.addPowerRight()));
        prescription.setProgressive(submission.progressive());
        prescription.setDocumentObjectKey(objectKey);
        prescription.setDocumentContentType(contentType);
        prescription.setVerificationStatus(VerificationStatus.PENDING_REVIEW);
        return prescription;
    }

    private String normaliseContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        String withoutParameters = contentType.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        return CONTENT_TYPE_ALIASES.getOrDefault(withoutParameters, withoutParameters);
    }

    private String objectKeyFor(String contentType) {
        return UUID.randomUUID() + EXTENSION_FOR_CONTENT_TYPE.getOrDefault(contentType, "");
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
