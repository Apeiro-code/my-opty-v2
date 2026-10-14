package com.myopty.billing.service;

/**
 * The seam that turns a settled payment or a refund into a message that leaves the
 * building.
 *
 * <p>{@link ReceiptService} decides <em>that</em> a receipt should go and gathers
 * its contents; the sender knows how to deliver it. Keeping it behind an interface
 * means a test can watch what would have been sent without an SMTP server, and a
 * move to a provider's own receipt API is a new implementation, not an edit to the
 * service.
 *
 * <p>A sender reports failure by throwing. The caller decides what that means — log
 * it, or surface it when configured to — because the payment is already recorded
 * either way.
 */
public interface ReceiptSender {

    /** Sends the receipt for a settled payment, with its invoice attached. */
    void sendPaymentReceipt(ReceiptEmail receipt);

    /** Sends confirmation that some or all of a payment was returned. */
    void sendRefundConfirmation(ReceiptEmail receipt);
}
