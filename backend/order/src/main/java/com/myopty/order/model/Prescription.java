package com.myopty.order.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A prescription as submitted by a customer.
 *
 * <p>One row per submission, never edited: reviewing produces a new value in
 * {@link #verificationStatus} rather than a different prescription, which is why
 * the optical values are ordinary fields and not something the caller updates.
 * The review decision is stamped with {@link #rejectionReason}, {@link #verifiedBy}
 * and {@link #verifiedAt}; the columns exist since V100 and are mapped here when
 * the review story starts writing them.
 *
 * <p>The optical values are {@code String}, not a number type, for the reason the
 * V100 migration gives: the optician writes {@code "+06.25"} and that exact text
 * is what reaches production, so parsing it into a {@code double} here would
 * quietly change a value read off a scan.
 *
 * <p>{@code is_progressive} maps to the field {@code progressive} rather than a
 * same-named field because a boolean getter named {@code getIsProgressive} reads
 * badly; the annotation keeps the column name explicit.
 */
@Table("prescription")
public class Prescription {

    @Id
    private Long id;

    private Long customerId;
    private String sphLeft;
    private String sphRight;
    private String cylLeft;
    private String cylRight;
    private String axisLeft;
    private String axisRight;
    private String addPowerLeft;
    private String addPowerRight;

    @Column("is_progressive")
    private boolean progressive;

    private String documentObjectKey;
    private String documentContentType;
    private VerificationStatus verificationStatus;
    private String rejectionReason;
    private Long verifiedBy;
    private LocalDateTime verifiedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSphLeft() {
        return sphLeft;
    }

    public void setSphLeft(String sphLeft) {
        this.sphLeft = sphLeft;
    }

    public String getSphRight() {
        return sphRight;
    }

    public void setSphRight(String sphRight) {
        this.sphRight = sphRight;
    }

    public String getCylLeft() {
        return cylLeft;
    }

    public void setCylLeft(String cylLeft) {
        this.cylLeft = cylLeft;
    }

    public String getCylRight() {
        return cylRight;
    }

    public void setCylRight(String cylRight) {
        this.cylRight = cylRight;
    }

    public String getAxisLeft() {
        return axisLeft;
    }

    public void setAxisLeft(String axisLeft) {
        this.axisLeft = axisLeft;
    }

    public String getAxisRight() {
        return axisRight;
    }

    public void setAxisRight(String axisRight) {
        this.axisRight = axisRight;
    }

    public String getAddPowerLeft() {
        return addPowerLeft;
    }

    public void setAddPowerLeft(String addPowerLeft) {
        this.addPowerLeft = addPowerLeft;
    }

    public String getAddPowerRight() {
        return addPowerRight;
    }

    public void setAddPowerRight(String addPowerRight) {
        this.addPowerRight = addPowerRight;
    }

    public boolean isProgressive() {
        return progressive;
    }

    public void setProgressive(boolean progressive) {
        this.progressive = progressive;
    }

    public String getDocumentObjectKey() {
        return documentObjectKey;
    }

    public void setDocumentObjectKey(String documentObjectKey) {
        this.documentObjectKey = documentObjectKey;
    }

    public String getDocumentContentType() {
        return documentContentType;
    }

    public void setDocumentContentType(String documentContentType) {
        this.documentContentType = documentContentType;
    }

    public VerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(VerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(Long verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}
